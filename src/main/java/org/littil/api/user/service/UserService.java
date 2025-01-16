package org.littil.api.user.service;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.littil.api.auth.provider.Provider;
import org.littil.api.auth.service.AuthUser;
import org.littil.api.auth.service.AuthenticationService;
import org.littil.api.auth.service.PasswordService;
import org.littil.api.auth.service.ProviderService;
import org.littil.api.exception.EntityAlreadyExistsException;
import org.littil.api.mail.MailService;
import org.littil.api.metrics.LittilMetrics;
import org.littil.api.user.repository.UserEntity;
import org.littil.api.user.repository.UserRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
@AllArgsConstructor
@Slf4j
public class UserService {

    AuthenticationService authenticationService;
    ProviderService providerService;
    UserRepository repository;
    MailService mailService;
    UserMapper mapper;
    PasswordService passwordService;

    public Optional<User> getUserById(UUID userId) {
        Optional<User> user = repository.findByIdOptional(userId).map(mapper::toDomain);
        return extendWithAuthDetails(user);
    }

    public Optional<User> getUserByProviderId(String providerId) {
        Optional<User> user = repository.findByProviderId(providerId).map(mapper::toDomain);
        return extendWithAuthDetails(user);
    }

    public Optional<User> getUserByEmailAddress(String email) {
        Optional<User> user = repository.findByEmailAddress(email).map(mapper::toDomain);
        return extendWithAuthDetails(user);
    }

    public List<User> listUsers() {
        List<AuthUser> authUsers = authenticationService.getAllUsers();
        // Convert to a Map for efficient lookup
        Map<String, AuthUser> authUserMap = authUsers.stream()
                .collect(Collectors.toMap(AuthUser::getProviderId, authUser -> authUser));

        return repository.findAll().stream()
                .map(mapper::toDomain)
                .map(user -> {
                    AuthUser authUser = authUserMap.get(user.getProviderId());
                    return mapper.updateDomainFromAuthUser(authUser, user);
                })
                .toList();
    }

    //todo can we refactor this method?
    @Transactional
    public User createUser(User user) {
        Optional<UserEntity> alreadyExistingUser = repository.findByEmailAddress(user.getEmailAddress());

        if(alreadyExistingUser.isPresent()) {
            log.warn("Failed to create user due to the fact that user with id " + user.getId()
                    +  " already has the same emailAddress.");
            throw new EntityAlreadyExistsException();
        }

        //save user locally
        if (user.getId() == null) {
            user.setId(UUID.randomUUID());
        }
        user.setProvider(providerService.whoAmI());
        UserEntity userEntity = mapper.toEntity(user);
        repository.persist(userEntity);
        mapper.updateDomainFromEntity(userEntity, user);

        // save user to the authentication provider as well
        String tempPassword = passwordService.generate();
        AuthUser createdUser = authenticationService.createUser(mapper.toAuthUser(user), tempPassword);
        mapper.updateEntityFromAuthUser(createdUser, userEntity);
        log.info(LittilMetrics.Registration.userCreatedInOidc());

        // update the user entity with the provider id
        repository.persist(userEntity);
        mapper.updateDomainFromEntity(userEntity, user);
        log.info(LittilMetrics.Registration.userPersisted());

        // send welcome mail with credentials
        mailService.sendWelcomeMail(user, tempPassword);
        log.info(LittilMetrics.Registration.mailSent());

        return user;
    }

    @Transactional
    public void deleteUser(UUID id) {
        Optional<UserEntity> user = repository.findByIdOptional(id);
        user.ifPresentOrElse(userEntity -> {
            authenticationService.deleteUser(userEntity.getId());
            repository.deleteById(userEntity.getId());
        }, () -> {
            throw new NotFoundException();
        });
    }

    @Transactional
    public void createAndPersistDevData(UUID id, String auth0id, String email) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setProvider(Provider.AUTH0);
        user.setProviderId(auth0id);
        user.setEmailAddress(email);
        this.repository.persist(user);
    }

    /*
     * Method can not be used in stream operation.
     * This will cause a com.auth0.exception.RateLimitException.
     * HttpStatus 429 (Too Many Requests) is returned by auth0.
     */
    private Optional<User> extendWithAuthDetails(Optional<User> user) {
        user.ifPresent(u ->
                authenticationService
                        .getUserById(u.getProviderId())
                        .ifPresent(authUser -> mapper.updateDomainFromAuthUser(authUser, u))
        );
        return user;
    }
}

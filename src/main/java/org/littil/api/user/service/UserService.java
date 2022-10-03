package org.littil.api.user.service;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.littil.api.auth.service.AuthUser;
import org.littil.api.auth.service.AuthenticationService;
import org.littil.api.auth.service.PasswordService;
import org.littil.api.auth.service.ProviderService;
import org.littil.api.mail.MailService;
import org.littil.api.exception.EntityAlreadyExistsException;
import org.littil.api.user.repository.UserEntity;
import org.littil.api.user.repository.UserRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import javax.ws.rs.NotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
        return repository.findByIdOptional(userId).map(mapper::toDomain);
    }

    public Optional<User> getUserByProviderId(String providerId) {
        return repository.findByProviderId(providerId).map(mapper::toDomain);
    }

    public List<User> listUsers() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
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
        user.setProvider(providerService.whoAmI());
        UserEntity userEntity = mapper.toEntity(user);
        repository.persist(userEntity);
        mapper.updateDomainFromEntity(userEntity, user);

        // save user to the authentication provider as well
        String tempPassword = passwordService.generate();
        AuthUser createdUser = authenticationService.createUser(mapper.toAuthUser(user), tempPassword);
        mapper.updateEntityFromAuthUser(createdUser, userEntity);

        // update the user entity with the provider id
        repository.persist(userEntity);
        mapper.updateDomainFromEntity(userEntity, user);

        // send welcome mail with credentials
        mailService.sendWelcomeMail(user, tempPassword);

        return user;
    }

    @Transactional
    public void deleteUser(UUID id) {
        Optional<UserEntity> user = repository.findByIdOptional(id);
        user.ifPresentOrElse(userEntity -> {
            authenticationService.deleteUser(userEntity.getId());
            repository.delete(userEntity);
        }, () -> {
            throw new NotFoundException();
        });
    }
}

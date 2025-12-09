package org.littil.api.user.service;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.littil.api.auth.provider.Provider;
import org.littil.api.auth.service.AuthUser;
import org.littil.api.auth.service.AuthenticationService;
import org.littil.api.auth.service.PasswordService;
import org.littil.api.auth.service.ProviderService;
import org.littil.api.exception.EntityAlreadyExistsException;
import org.littil.api.exception.VerificationCodeException;
import org.littil.api.mail.MailService;
import org.littil.api.metrics.LittilMetrics;
import org.littil.api.user.api.ChangeEmailResource;
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
    VerificationCodeService verificationCodeService;
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
            authenticationService.deleteUser(userEntity.getProviderId());
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
     * It will be throttled by Auth0 (com.auth0.exception.RateLimitException)
     */
    private Optional<User> extendWithAuthDetails(Optional<User> user) {
        user.ifPresent(u ->
                authenticationService
                        .getUserById(u.getProviderId())
                        .ifPresent(authUser -> mapper.updateDomainFromAuthUser(authUser, u))
        );
        return user;
    }

    public void sendVerificationCode(UUID userId, String emailAddress) {
        validateEmailIsAvailable(emailAddress);
        VerificationCode verificationCode = verificationCodeService.getVerificationCode(userId, emailAddress);
    	mailService.sendVerificationCode(verificationCode);
    }

    @Transactional
	public void changeEmail(UUID userId, ChangeEmailResource changeEmailResource) {
    	String newEmailAddress = getEmailAddressIfTokenIsValid(userId, changeEmailResource);
        validateEmailIsAvailable(newEmailAddress);
        Optional<User> optionalUser = getUserById(userId);
		  if (optionalUser.isEmpty()) {
	            throw new NotFoundException("User with id " + userId + " not found.");
		  }

		  User user  = optionalUser.get();
          user.setEmailAddress(newEmailAddress);

          this.repository.getEntityManager().merge(mapper.toEntity(user));
          authenticationService.changeEmailAddress(user.getProviderId(), newEmailAddress);
	}

    /**
     * Validates that the given email address is not already associated with an existing user.
     * <p>
     * This method queries the user repository by the provided email address. If a user is found,
     * it logs a warning and throws an {@link EntityAlreadyExistsException}. Use this method
     * before creating a new user or changing a user's email to enforce uniqueness.
     * </p>
     *
     * @param emailAddress the email address to check; must be a non-null, non-blank string
     * @throws EntityAlreadyExistsException if the provided {@code emailAddress} is already in use
     */
    private void validateEmailIsAvailable(String emailAddress) {
        Optional<User> alreadyExistingUser = getUserByEmailAddress(emailAddress);
        if(alreadyExistingUser.isPresent()) {
            log.warn("Failed to change email address due to the fact that user with id " + alreadyExistingUser.get().getId()
                    +  " already has the same emailAddress.");
            throw new EntityAlreadyExistsException();
        }
    }

    /**
     * Returns the new email address from the given {@link ChangeEmailResource} if the provided
     * verification token is valid for the specified user and email address.
     * <p>
     * This method delegates token validation to {@code verificationCodeService.isValidToken(userId, emailAddress, verificationCode)}.
     * If the token is valid, the new email address is returned. Otherwise, a {@link VerificationCodeException}
     * is thrown to indicate an invalid or expired verification code.
     * </p>
     *
     * @param userId the identifier of the user requesting the email change; must not be {@code null}
     * @param changeEmailResource the DTO containing the new email address and verification code; must not be {@code null}
     * @return the new email address if the verification token is valid
     * @throws VerificationCodeException if the verification code is invalid, mismatched, or expired
     */
    private String getEmailAddressIfTokenIsValid(UUID userId, ChangeEmailResource changeEmailResource) {
		String emailAddress = changeEmailResource.getNewEmailAddress();
		String verificationCode = changeEmailResource.getVerificationCode();
		if (verificationCodeService.isValidToken(userId, emailAddress, verificationCode)) {
			return changeEmailResource.getNewEmailAddress();
		}
		throw new VerificationCodeException("verification code is not valid");
	}
}

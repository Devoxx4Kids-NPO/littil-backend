package org.littil.api;

import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.client.mgmt.filter.UserFilter;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.users.User;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.configuration.ProfileManager;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.littil.api.auth.provider.Provider;
import org.littil.api.auth.service.AuthorizationType;
import org.littil.api.exception.EntityAlreadyExistsException;
import org.littil.api.user.repository.UserEntity;
import org.littil.api.user.repository.UserRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

@ApplicationScoped
@Slf4j
public class ApplicationLifeCycle {

    //TODO: make this configurable with other purposes
    private static final List<String> DEV_USERS = List.of(
            "info@littil.org",
            "testdocent1@littil.org",
            "testdocent2@littil.org",
            "testdocent3@littil.org",
            "testschool1@littil.org",
            "testschool2@littil.org",
            "testschool3@littil.org"
    );
    @Inject
    @ConfigProperty(name = "org.littil.devservices.devdata", defaultValue = "false")
    boolean insertDevData;

    @Inject
    @ConfigProperty(name = "org.littil.auth.token.claim.user_id")
    String userIdClaimName;

    @Inject
    @ConfigProperty(name = "org.littil.auth.token.claim.authorizations")
    String authorizationsClaimName;

    @Inject
    UserRepository userRepository;

    @Inject
    ManagementAPI managementAPI;

    void onStart(@Observes StartupEvent ev) {
        log.info("The application is starting with profile {}",ProfileManager.getActiveProfile());
        if (this.insertDevData) {
            persistDevData();
        }
    }

    void persistDevData() {
        log.info("Persisting auth0 user data to datasource, this should not be happening in staging nor production.");
        DEV_USERS.stream()
                .flatMap(this::persistDevUserData)
                .map(UserEntity::getEmailAddress)
                .forEach(email -> log.info("Created {} user for development purposes",email));
        log.info("Added general LiTTiL users for development purposes. You can login via email-addresses and the default password to the dev tenant.");
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    Stream<UserEntity> persistDevUserData(String email) {
           return this.findAuth0User(email)
                .map(this::toUserEntity)
                .flatMap(Optional::stream)
                .peek(this.userRepository::persist);
    }

    private Stream<User> findAuth0User(String email) {
        try {
            return this.managementAPI
                    .users().listByEmail(email,new UserFilter())
                    .execute()
                    .stream();
        } catch (Auth0Exception e) {
            log.error("unable to find {} in auth0 ",email,e);
            return Stream.empty();
        }
    }

    protected Optional<UserEntity> toUserEntity(User user) {
        var appMetaData = Optional.ofNullable(user)
                .map(User::getAppMetadata)
                .filter(data -> data.containsKey(this.userIdClaimName))
                .filter(data -> data.containsKey(this.authorizationsClaimName))
                .orElse(Collections.emptyMap());

        // incomplete/invalid user
        if(appMetaData.isEmpty()) {
            return Optional.empty();
        }
        var auth0id = user.getId();

        var userId = UUID.fromString(String.valueOf(appMetaData.get(this.userIdClaimName)));
        if(this.userRepository.findByIdOptional(userId).isPresent()) {
            log.warn("Not creating user[{}] for dev; duplicate id {}",auth0id,userId);
            return Optional.empty();
        } else if(this.userRepository.findByEmailAddress(user.getEmail()).isPresent()) {
            log.warn("Not creating user[{}] for dev; duplicate email {}",auth0id,user.getEmail());
            return Optional.empty();
        }

        var userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setProvider(Provider.AUTH0);
        userEntity.setProviderId("auth0|"+auth0id);
        userEntity.setEmailAddress(user.getEmail());

        var schoolId = getAuthorizationClaim(appMetaData,AuthorizationType.SCHOOL);
        var teacherId = getAuthorizationClaim(appMetaData,AuthorizationType.GUEST_TEACHER);

        return Optional.of(userEntity);
    }

    private Optional<UUID> getAuthorizationClaim(Map<String,Object> appMetaData,AuthorizationType type) {
        Map<String,Object> authorizations = (Map<String, Object>) appMetaData.get(this.authorizationsClaimName);
        List<UUID> value = Optional.ofNullable((List)authorizations.get(type.getTokenValue())).orElse(Collections.emptyList());
        return value.stream().findFirst();
    }
}
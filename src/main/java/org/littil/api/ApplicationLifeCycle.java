package org.littil.api;

import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.client.mgmt.filter.FieldsFilter;
import com.auth0.client.mgmt.filter.UserFilter;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.users.User;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.configuration.ProfileManager;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.littil.api.auth.provider.Provider;
import org.littil.api.auth.service.AuthorizationType;
import org.littil.api.user.repository.UserEntity;
import org.littil.api.user.repository.UserRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

@ApplicationScoped
@Slf4j
public class ApplicationLifeCycle {

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

    void onStart(@Observes StartupEvent ev) throws Auth0Exception {
        log.info("The application is starting with profile {}",ProfileManager.getActiveProfile());
        if (insertDevData) {
            persistDevData();
        }
    }

    //todo fix this. This won't work when developing simultaneously
    private void cleanLittilAuth0User() throws Auth0Exception {
        log.info("Removing all authorizations from Auth0 appMetadata, this might cause issues when 2 developers are testing at once.");

        User user = new User();
        String userId = "auth0|62fd3224f76949850e9eb264";
        Map<String, Object> appMetadata = Map.of(userIdClaimName, "e87cda48-9a98-41a7-89bb-9ee62441c84c",
                authorizationsClaimName, Map.of(AuthorizationType.SCHOOL.getTokenValue(), emptyList(), AuthorizationType.GUEST_TEACHER.getTokenValue(), emptyList()));
        user.setAppMetadata(appMetadata);
        managementAPI.users().update(userId, user).execute();

        log.info("Done all authorizations from Auth0 appMetadata, this might cause issues when 2 developers are testing at once.");
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    void persistDevData() {
        log.info("Persisting development data to datasource, this should not be happening in staging nor production.");
        auth0users()
                .flatMap(this::persistLocal)
                .map(UserEntity::getEmailAddress)
                .forEach(email -> log.info("Created {} user for development purposes",email));
        log.info("Added general LiTTiL users for development purposes. You can login via email-addresses and the default password to the dev tenant.");
    }

    private Stream<UserEntity> devUsers() {
        return Stream.of(
                create("62fd3224f76949850e9eb264","info@littil.org")
        );
    }

    private UserEntity create(String auth0Id, String email) {
        var user = new UserEntity();
        //user.setId(UUID.from(e87cda48-9a98-41a7-89bb-9ee62441c84c)) // old fixed id
        user.setProvider(Provider.AUTH0);
        user.setProviderId("auth0|"+auth0Id);
        user.setEmailAddress(email);
        return user;
    }

    private Stream<UserEntity> persistLocal(User user) {
        var appMetaData = user.getAppMetadata();
        if(!appMetaData.containsKey(this.userIdClaimName) && !appMetaData.containsKey(this.authorizationsClaimName)) {
            log.debug("Ignored {} user for development purposes",user.getEmail());
            return Stream.empty();
        }
        var uuid = UUID.fromString(String.valueOf(user.getAppMetadata().get(this.userIdClaimName)));
        var persistedUser = new UserEntity();
        persistedUser.setId(uuid);
        persistedUser.setProvider(Provider.AUTH0);
        persistedUser.setProviderId("auth0|"+user.getId());
        persistedUser.setEmailAddress(user.getEmail());
        this.userRepository.persist(persistedUser);
        return Stream.of(persistedUser);
    }

    private Stream<User> auth0users() {
        try {
            return this.managementAPI
                    .users()
                    .list(new UserFilter())
                    .execute()
                    .getItems()
                    .stream();
        } catch (Auth0Exception e) {
            log.error("unable to get users from auth0 ",e);
            return Stream.empty();
        }
    }
}

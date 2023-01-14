package org.littil.api;

import com.auth0.client.mgmt.ManagementAPI;
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
            cleanLittilAuth0User();
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
        devUsers()
                .peek(this.userRepository::persist)
                .map(UserEntity::getEmailAddress)
                .forEach(email -> log.info("Created {} user for development purposes",email));
        log.info("Added general LiTTiL users for development purposes. You can login via email-addresses and the default password to the dev tenant.");
    }

    private Stream<UserEntity> devUsers() {
        return Stream.of(
                create(UUID.fromString("e87cda48-9a98-41a7-89bb-9ee62441c84c"),"62fd3224f76949850e9eb264","info@littil.org"),
                create(UUID.fromString("3fc95e78-b498-4d20-bc60-c6a672b8931f"),"63c2ba7ac4d4beeea5ad3d4e","testdocent1@littil.org"),
                create(UUID.fromString("312badca-44c4-499b-a92a-df1c16b30877"),"63c2baa6efc9a808a903f2e9","testdocent2@littil.org"),
                create(UUID.fromString("e7164fe3-52b3-4ac5-a2bf-f6ce085bea38"),"63c2bab9b4c082fb977f270e","testdocent3@littil.org"),
                create(UUID.fromString("50e2b86b-51ed-4611-936b-d16adcb0b104"),"63c2baca2333c1e8b1545a54","testschool1@littil.org"),
                create(UUID.fromString("72335590-78c0-4810-b793-cce4e22eff9a"),"63c2bad91fa0489a010f1ba3","testschool2@littil.org"),
                create(UUID.fromString("6e0d0eb8-e187-4bde-a4f3-86a97fc66f65"),"63c2baf02333c1e8b1545a5a","testschool3@littil.org")
        );
    }

    private UserEntity create(UUID id, String auth0Id, String email) {
        var user = new UserEntity();
        user.setId(id);
        user.setProvider(Provider.AUTH0);
        user.setProviderId("auth0|"+auth0Id);
        user.setEmailAddress(email);
        return user;
    }
}

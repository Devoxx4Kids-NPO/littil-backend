package org.littil.api;

import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.client.mgmt.filter.UserFilter;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.users.User;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.configuration.ProfileManager;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.littil.api.auth.service.AuthorizationType;
import org.littil.api.guestTeacher.service.GuestTeacherService;
import org.littil.api.school.service.SchoolService;
import org.littil.api.user.service.UserService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@ApplicationScoped
@Slf4j
public class ApplicationLifeCycle {

    //TODO: make this configurable for other purposes (e2e tests for example)
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
    UserService userService;

    @Inject
    SchoolService schoolService;

    @Inject
    GuestTeacherService guestTeacherService;

    @Inject
    ManagementAPI managementAPI;

    void onStart(@Observes StartupEvent ev) {
        if (this.insertDevData && ProfileManager.getLaunchMode().isDevOrTest()) {
            persistDevData();
        }
    }

    void persistDevData() {
        log.info("Persisting auth0 user data to datasource, this should not be happening in staging nor production.");
        DEV_USERS.stream()
                .flatMap(this::persistDevUserData)
                .forEach(email -> log.info("Created {} user for development purposes", email));
        log.info("Added general LiTTiL users for development purposes. You can login via email-addresses and the default password to the dev tenant.");
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    Stream<String> persistDevUserData(String email) {
           return this.findAuth0User(email)
                .map(this::createAndPersistDevData)
                .flatMap(Optional::stream);
    }

    private Stream<User> findAuth0User(String email) {
        try {
            return this.managementAPI
                    .users().listByEmail(email,new UserFilter())
                    .execute()
                    .getBody()
                    .stream();
        } catch (Auth0Exception e) {
            log.error("unable to find {} in auth0, skipping this development user ", email, e);
            return Stream.empty();
        }
    }

    protected Optional<String> createAndPersistDevData(User user) {
        var appMetaData = Optional.ofNullable(user)
                .map(User::getAppMetadata)
                .filter(data -> data.containsKey(this.userIdClaimName))
                .filter(data -> data.containsKey(this.authorizationsClaimName))
                .orElse(Collections.emptyMap());

        var auth0id = user.getId();
        if (appMetaData.isEmpty()) {
            log.warn("Auth0 user [{}] incomplete metadata, missing {} or {}. Unable to create mapping with local user, school or guest teacher. Skipping this development user.", auth0id, this.userIdClaimName, this.authorizationsClaimName);
            return Optional.empty();
        }
        var userId = UUID.fromString(String.valueOf(appMetaData.get(this.userIdClaimName)));
        var authorizations = (Map<String, List<String>>) appMetaData.get(this.authorizationsClaimName);
        return createAndPersistDevData(userId, auth0id, user.getEmail(), authorizations);
    }

    protected Optional<String> createAndPersistDevData(UUID userId, String auth0id, String email, Map<String, List<String>> authorizations) {
        if (this.userService.getUserById(userId).isPresent()) {
            log.warn("Not creating user[{}] for dev; An user with id {} already exists", auth0id, userId);
            return Optional.empty();
        }

        if (this.userService.getUserByEmailAddress(email).isPresent()) {
            log.warn("Not creating user[{}] for dev; An user with email address {} already exists.", auth0id, email);
            return Optional.empty();
        }

        this.userService.createAndPersistDevData(userId, auth0id, email);
        AuthorizationType.SCHOOL.authorizationIds(authorizations)
                .findFirst().ifPresent(schoolId -> schoolService.createAndPersistDevData(schoolId, userId));
        AuthorizationType.GUEST_TEACHER.authorizationIds(authorizations)
                .findFirst().ifPresent(teacherId -> guestTeacherService.createAndPersistDevData(teacherId, userId));
        return Optional.of(email);
    }
}
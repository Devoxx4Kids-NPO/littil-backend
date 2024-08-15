package org.littil.api;

import com.auth0.client.mgmt.filter.UserFilter;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.users.User;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.LaunchMode;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.littil.api.auth.provider.auth0.Auth0ManagementAPI;
import org.littil.api.auth.service.AuthorizationType;
import org.littil.api.guestTeacher.service.GuestTeacherService;
import org.littil.api.location.Location;
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
    // use locations from public places like city town hall, police station, railway station, etc.
    private static final Map<String, Location> DEV_USERS = Map.of(
            "info@littil.org", new Location(), //
            "testdocent1@littil.org", createLocationForDevUser("De Passage 100", "1101AX"), //
            "testdocent2@littil.org", createLocationForDevUser("Colosseum 65", "7521PP"), //
            "testdocent3@littil.org", createLocationForDevUser("Mosae Forum 10",  "6211DW"), //
            "testschool1@littil.org", createLocationForDevUser("Prinses Irenepad 1", "2595BG"), //
            "testschool2@littil.org", createLocationForDevUser("Marco Pololaan 6", "3526GJ"), //
            "testschool3@littil.org", createLocationForDevUser("Sint Jansstraat 4", "9712JN") //
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
    Auth0ManagementAPI auth0api;

    void onStart(@Observes StartupEvent ev) {
        if (this.insertDevData) {
            persistDevData();
        } else {
            log.info("Skip persisting auth0 user data for development");
        }
    }

    void persistDevData() {
        log.info("Persisting auth0 user data to datasource, this should not be happening in staging nor production. LaunchMode: {}",LaunchMode.current());
        DEV_USERS.entrySet().stream()
                .flatMap(this::persistDevUserData)
                .forEach(email -> log.info("Created {} user for development purposes", email));
        log.info("Added general LiTTiL users for development purposes. You can login via email-addresses and the default password to the dev tenant.");
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    Stream<String> persistDevUserData(Map.Entry<String, Location> emailLocationEntry) {
        String email = emailLocationEntry.getKey();
        Location location = emailLocationEntry.getValue();

           return this.findAuth0User(email)
                .map(user -> createAndPersistDevData(user,location))
                .flatMap(Optional::stream);
    }

    private Stream<User> findAuth0User(String email) {
        try {
            return auth0api
                    .users().listByEmail(email,new UserFilter())
                    .execute()
                    .getBody()
                    .stream();
        } catch (Auth0Exception e) {
            log.error("unable to find {} in auth0, skipping this development user ", email, e);
            return Stream.empty();
        }
    }

    protected Optional<String> createAndPersistDevData(User user, Location location) {
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
        return createAndPersistDevData(userId, auth0id, user.getEmail(), authorizations, location);
    }

    protected Optional<String> createAndPersistDevData(UUID userId, String auth0id, String email, Map<String, List<String>> authorizations, Location location) {
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
                .findFirst().ifPresent(schoolId -> schoolService.createAndPersistDevData(schoolId, userId, email, location));
        AuthorizationType.GUEST_TEACHER.authorizationIds(authorizations)
                .findFirst().ifPresent(teacherId -> guestTeacherService.createAndPersistDevData(teacherId, userId, email,location));
        return Optional.of(email);
    }

    private static Location createLocationForDevUser(String address, String postalCode) {
        Location location = new Location();
        location.setCountry("Nederland");
        location.setAddress(address);
        location.setPostalCode(postalCode);
        return location;
    }

}

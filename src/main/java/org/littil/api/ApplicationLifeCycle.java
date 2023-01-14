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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.*;
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
        log.info("The application is starting with profile {}",ProfileManager.getActiveProfile());
        if (this.insertDevData) {
            persistDevData();
        }
    }

    void persistDevData() {
        log.info("Persisting auth0 user data to datasource, this should not be happening in staging nor production.");
        DEV_USERS.stream()
                .flatMap(this::persistDevUserData)
                .forEach(email -> log.info("Created {} user for development purposes",email));
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
                    .stream();
        } catch (Auth0Exception e) {
            log.error("unable to find {} in auth0 ",email,e);
            return Stream.empty();
        }
    }

    protected Optional<String> createAndPersistDevData(User user) {
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
        if(this.userService.getUserById(userId).isPresent()) {
            log.warn("Not creating user[{}] for dev; duplicate id {}",auth0id,userId);
            return Optional.empty();
        } else if(this.userService.getUserByEmailAddress(user.getEmail()).isPresent()) {
            log.warn("Not creating user[{}] for dev; duplicate email {}",auth0id,user.getEmail());
            return Optional.empty();
        }
        this.userService.createAndPersistDevData(userId, auth0id, user.getEmail());
        getAuthorizationClaim(appMetaData,AuthorizationType.SCHOOL)
                .ifPresent(schoolId -> schoolService.createAndPersistDevData(schoolId,userId));
        getAuthorizationClaim(appMetaData,AuthorizationType.GUEST_TEACHER)
                .ifPresent(teacherId -> guestTeacherService.createAndPersistDevData(teacherId,userId));
        return Optional.of(user.getEmail());
    }

    private Optional<UUID> getAuthorizationClaim(Map<String,Object> appMetaData,AuthorizationType type) {
        Map<String,Object> authorizations = (Map<String, Object>) appMetaData.get(this.authorizationsClaimName);
        List<UUID> value = Optional.ofNullable((List)authorizations.get(type.getTokenValue())).orElse(Collections.emptyList());
        return value.stream().findFirst();
    }
}
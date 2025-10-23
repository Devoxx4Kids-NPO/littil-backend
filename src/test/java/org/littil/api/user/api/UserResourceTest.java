package org.littil.api.user.api;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.restassured.http.ContentType;
import org.littil.RandomStringGenerator;
import org.junit.jupiter.api.Test;
import org.littil.TestFactory;
import org.littil.api.auth.TokenHelper;
import org.littil.api.auth.provider.Provider;
import org.littil.api.auth.service.AuthUser;
import org.littil.api.auth.service.AuthenticationService;
import org.littil.api.user.service.User;
import org.littil.api.user.service.UserService;
import org.littil.api.user.service.VerificationCode;
import org.littil.api.user.service.VerificationCodeService;
import org.littil.mock.auth0.APIManagementMock;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestHTTPEndpoint(UserResource.class)
@QuarkusTestResource(APIManagementMock.class)
class UserResourceTest {

    @InjectMock
    AuthenticationService authenticationService;

    @InjectMock
    VerificationCodeService verificationCodeService;

    @InjectSpy
    UserService userService;

    @InjectMock
    TokenHelper tokenHelper;

    @Test
    void givenFindAllUnauthorized_thenShouldReturnForbidden() {
        given()
                .when()
                .get("/user")
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "littil", roles = "admin")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenList_thenShouldReturnMultipleUsers() {
        given()
                .when()
                .get("/user")
                .then()
                .statusCode(200);
    }

    @Test
    @TestSecurity(user = "littil", roles = "admin")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenGetUserById_thenShouldReturnSuccessfully() {
        UserPostResource user = getDefaultUser();
        AuthUser authUser = getAuthUser(user);
        User saved = saveUser(user, authUser);

        when(authenticationService.getUserById(any())).thenReturn(Optional.of(authUser));

        User got = given()
                .when()
                .get("/user/{id}", saved.getId())
                .then()
                .statusCode(200)
                .extract().as(User.class);
        assertThat(saved).isEqualTo(got);
    }

    @Test
    @TestSecurity(user = "littil", roles = "admin")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenGetUserByUnknownId_thenShouldReturnNotFound() {
        given()
                .when()
                .get("/user/{id}", UUID.randomUUID())
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenGetUserByProviderId_thenShouldReturnSuccessfully() {
        String providerId = RandomStringGenerator.generate(10);
        User saved = createAndSaveUser(providerId);

        doReturn(saved.getId()).when(tokenHelper).getCurrentUserId();

        User got = given()
                .when()
                .get("/user/provider/{id}", providerId)
                .then()
                .statusCode(200)
                .extract().as(User.class);

        assertThat(saved).isEqualTo(got);
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenGetUnownedUserByProviderId_thenShouldReturnForbidden() {
        String providerId = RandomStringGenerator.generate(10);
        UUID otherUserId = UUID.randomUUID();
        createAndSaveUser(providerId);

        doReturn(otherUserId).when(tokenHelper).getCurrentUserId();

        given()
                .when()
                .get("/user/provider/{id}", providerId)
                .then()
                .statusCode(403);
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenGetUserByUnknownProviderId_thenShouldReturnNotFound() {
        String providerId = RandomStringGenerator.generate(10);
        String otherProviderId = RandomStringGenerator.generate(10);
        User saved = createAndSaveUser(providerId);

        doReturn(saved.getId()).when(tokenHelper).getCurrentUserId();

        given()
                .when()
                .get("/user/provider/{id}", otherProviderId)
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "littil", roles = "admin")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenCreateNewUser_thenShouldBeCreatedSuccessfully() {
        UserPostResource user = getDefaultUser();
        User saved = saveUser(user);
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    @TestSecurity(user = "littil", roles = "admin")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenCreateNewUserWithExistingEmail_thenShouldReturnConflict() {
        UserPostResource user = getDefaultUser();
        saveUser(user);
        given()
                .contentType(ContentType.JSON)
                .body(user)
                .post("/user")
                .then()
                .statusCode(409);
    }

    @Test
    @TestSecurity(user = "littil", roles = "admin")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenCreateUserWithoutEmail_thenShouldReturnBadRequest() {
        UserPostResource user = new UserPostResource();
        given()
                .contentType(ContentType.JSON)
                .body(user)
                .post("/user")
                .then()
                .statusCode(400);
    }

    @Test
    @TestSecurity(user = "littil", roles = "admin")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenDeleteUserById_thenShouldDeleteSuccessfully() {
        UserPostResource user = getDefaultUser();
        User saved = saveUser(user);

        given()
                .contentType(ContentType.JSON)
                .delete("/user/{id}", saved.getId())
                .then()
                .statusCode(200);

        given()
                .contentType(ContentType.JSON)
                .get("/user/{id}", saved.getId())
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "littil", roles = "admin")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenDeleteNonExistingUserById_thenShouldReturnNotFound() {
        given()
                .contentType(ContentType.JSON)
                .delete("/user/{id}", UUID.randomUUID())
                .then()
                .statusCode(404);
    }

    @Test
    void givenGetUserStatisticsUnauthorized_thenShouldReturnForbidden() {
        given()
                .when()
                .get("/statistics")
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "littil", roles = "admin")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenGetUserStatisticsWithAdminRole_thenShouldReturnSuccesfully() {
        given()
                .when()
                .get("/statistics")
                .then()
                .statusCode(200);
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenGetUserStatisticsWithoutAdminRole_thenShouldReturnForbidden() {
        given()
                .when()
                .get("/statistics")
                .then()
                .statusCode(403);
    }

    @Test
    @TestSecurity(user = "littil", roles = "school")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenPostSendEmailWithVerificationCode_thenShouldReturnNoContent() {

        String email = "email@littil.org";
        EmailVerficationResource emailVerficationResource = new EmailVerficationResource();
        emailVerficationResource.setEmailAddress(email);
        UUID userId = UUID.fromString("0ea41f01-cead-4309-871c-c029c1fe19bf");

        doReturn(userId).when(tokenHelper).getCurrentUserId();
        doReturn(new VerificationCode(email)).when(verificationCodeService).getVerificationCode(any());

        given()
                .when()
                .contentType(ContentType.JSON)
                .body(emailVerficationResource)
                .post("/user/{id}/email/verification", userId)
                .then()
                .statusCode(204);
    }

    @Test
    @TestSecurity(user = "littil", roles = "guest_teacher")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenPostSendEmailWithVerificationCodeForExistingEmail_thenShouldReturnConflict() {

        EmailVerficationResource emailVerficationResource = new EmailVerficationResource();
        emailVerficationResource.setEmailAddress("email@littil.org");
        UUID userId = UUID.fromString("0ea41f01-cead-4309-871c-c029c1fe19bf");

        doReturn(userId).when(tokenHelper).getCurrentUserId();
        when(verificationCodeService.getVerificationCode(any()))
                .thenThrow(new IllegalArgumentException("Verification process still in progress"));

        given()
                .when()
                .contentType(ContentType.JSON)
                .body(emailVerficationResource)
                .post("/user/{id}/email/verification", userId)
                .then()
                .statusCode(409);
    }

    @Test
    @TestSecurity(user = "littil", roles = "guest_teacher")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenPostSendEmailWithVerificationCodeWithOtherUserId_thenShouldReturnNotAuthorized() {

        UUID userId = UUID.fromString("0ea41f01-cead-4309-871c-c029c1fe19bf");
        UUID otherUserId = UUID.randomUUID();

        doReturn(otherUserId).when(tokenHelper).getCurrentUserId();

        given()
                .when()
                .contentType(ContentType.JSON)
                .body(new EmailVerficationResource())
                .post("/user/{id}/email/verification", userId)
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenPostSendEmailWithVerificationCodeUnauthorized_thenShouldReturnNotAuthorized() {

        given()
                .when()
                .contentType(ContentType.JSON)
                .body(new EmailVerficationResource())
                .post("/user/{id}/email/verification", UUID.randomUUID())
                .then()
                .statusCode(403);
    }

    @Test
    @TestSecurity(user = "littil", roles = "school")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenPatchChangeEmailSchools_thenShouldReturnOK() {

        String newEmail = "new-email@littil.org";
        ChangeEmailResource resource = new ChangeEmailResource();
        resource.setNewEmailAddress(newEmail);
        resource.setVerificationCode("secret");
        UUID userId = UUID.fromString("0ea41f01-cead-4309-871c-c029c1fe19bf");

        doReturn(userId).when(tokenHelper).getCurrentUserId();
        doReturn(true   ).when(verificationCodeService).isValidToken(any(),any());

        given()
                .when()
                .contentType(ContentType.JSON)
                .body(resource)
                .patch("/user/{id}/email", userId)
                .then()
                .statusCode(200);
    }

    @Test
    @TestSecurity(user = "littil", roles = "school")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenPatchChangeEmailOtherUserId_thenShouldReturnUnauthorized() {

        UUID userId = UUID.fromString("0ea41f01-cead-4309-871c-c029c1fe19bf");
        UUID otherUserId = UUID.randomUUID();

        doReturn(userId).when(tokenHelper).getCurrentUserId();

        given()
                .when()
                .contentType(ContentType.JSON)
                .body(new ChangeEmailResource())
                .patch("/user/{id}/email", otherUserId)
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "littil", roles = "school")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenPatchChangeEmailForExistingEmail_thenShouldReturnConflict() {

        String existingEmail = createAndSaveUser(UUID.randomUUID().toString()).getEmailAddress();

        ChangeEmailResource resource = new ChangeEmailResource();
        resource.setNewEmailAddress(existingEmail);
        resource.setVerificationCode("secret");
        UUID userId = UUID.fromString("0ea41f01-cead-4309-871c-c029c1fe19bf");

        doReturn(userId).when(tokenHelper).getCurrentUserId();
        doReturn(true   ).when(verificationCodeService).isValidToken(any(),any());

        given()
                .when()
                .contentType(ContentType.JSON)
                .body(resource)
                .patch("/user/{id}/email", userId)
                .then()
                .statusCode(409);
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenPatchChangeEmailUnauthorized_thenShouldReturnNotAuthorized() {

        given()
                .when()
                .contentType(ContentType.JSON)
                .body(new ChangeEmailResource())
                .patch("/user/{id}/email", UUID.randomUUID())
                .then()
                .statusCode(403);
    }

    private UserPostResource getDefaultUser() {
        UserPostResource user = new UserPostResource();
        user.setEmailAddress(RandomStringGenerator.generate(10) + "@littil.org");
        return user;
    }

    private AuthUser getAuthUser(UserPostResource user) {
        AuthUser authUser = new AuthUser();
        authUser.setEmailAddress(user.getEmailAddress());
        authUser.setProvider(Provider.AUTH0);
        authUser.setRoles(Set.of("role1", "role2"));
        return authUser;
    }

    private User saveUser(UserPostResource user, AuthUser authUser) {
        User savedUser = saveUser(user);
        savedUser.setRoles(authUser.getRoles());
        return savedUser;
    }

    private User saveUser(UserPostResource user) {
        return given()
                .contentType(ContentType.JSON)
                .body(user)
                .post("/user")
                .then()
                .statusCode(201)
                .extract().as(User.class);
    }

    private User createAndSaveUser(String providerId) {
        User user = TestFactory.createUser();
        user.setProviderId(providerId);
        return userService.createUser(user);
    }
}
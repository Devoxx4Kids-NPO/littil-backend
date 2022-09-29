package org.littil.api.user.api;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.restassured.http.ContentType;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.littil.api.auth.service.AuthenticationService;
import org.littil.api.user.service.User;
import org.littil.mock.auth0.APIManagementMock;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestHTTPEndpoint(UserResource.class)
@QuarkusTestResource(APIManagementMock.class)
class UserResourceTest {

    @InjectMock
    AuthenticationService authenticationService;

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
        User saved = saveUser(user);

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

    private UserPostResource getDefaultUser() {
        UserPostResource user = new UserPostResource();
        user.setEmailAddress(RandomStringUtils.randomAlphabetic(10) + "@adres.nl");
        return user;
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
}
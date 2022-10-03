package org.littil.api.guestTeacher.api;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.restassured.http.ContentType;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.littil.api.auth.service.AuthenticationService;
import org.littil.api.coordinates.service.Coordinates;
import org.littil.api.coordinates.service.CoordinatesService;
import org.littil.api.exception.ErrorResponse;
import org.littil.api.guestTeacher.service.GuestTeacher;
import org.littil.api.user.service.User;
import org.littil.api.user.service.UserService;
import org.littil.mock.auth0.APIManagementMock;

import java.time.DayOfWeek;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.littil.Helper.getErrorMessage;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

@QuarkusTest
@TestHTTPEndpoint(GuestTeacherResource.class)
@QuarkusTestResource(APIManagementMock.class)
class GuestTeacherResourceTest {

    @InjectSpy
    UserService userService;

    @InjectMock
    CoordinatesService coordinatesService;

    @InjectMock
    AuthenticationService authenticationService;

    @Test
    void givenFindAllUnauthorized_thenShouldReturnForbidden() {
        given()
                .when()
                .get()
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenFindAll_thenShouldReturnMultipleTeachers() {
        given()
                .when()
                .get()
                .then()
                .statusCode(200);
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenGetTeacherById_thenShouldReturnSuccessfully() {
        GuestTeacherPostResource teacher = getGuestTeacherPostResource();
        GuestTeacher saved = saveTeacher(teacher);

        GuestTeacher got = given()
                .when()
                .get("/{id}", saved.getId())
                .then()
                .statusCode(200)
                .extract().as(GuestTeacher.class);

        assertThat(saved).isEqualTo(got);
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenGetTeacherByUnknownId_thenShouldReturnNotFound() {
        given()
                .when()
                .get("/{id}", UUID.randomUUID())
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenGetTeacherByName_thenShouldReturnSuccessfully() {
        String validSurname = RandomStringUtils.randomAlphabetic(10);
        GuestTeacherPostResource teacher = getGuestTeacherPostResource();
        teacher.setSurname(validSurname);
        GuestTeacher saved = saveTeacher(teacher);

        List<GuestTeacher> got = given()
                .when()
                .get("/name/{name}", validSurname)
                .then()
                .statusCode(200)
                .extract()
                .jsonPath().getList(".", GuestTeacher.class);

        assertThat(saved).isIn(got);
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenGetTeacherByUnknownName_thenShouldReturnNotFound() {
        given()
                .when()
                .get("/{name}", RandomStringUtils.randomAlphabetic(10))
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenCreateNewTeacher_thenShouldBeCreatedSuccessfully() {
        GuestTeacherPostResource teacher = getGuestTeacherPostResource();
        GuestTeacher saved = saveTeacher(teacher);

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenCreateNewTeacherWithoutRequiredNames_thenShouldReturnWithAnErrorResponse() {
        GuestTeacherPostResource teacher = getGuestTeacherPostResource();
        teacher.setSurname(null);
        teacher.setFirstName(null);

        User createdUser = createAndSaveUser();
        doReturn(Optional.ofNullable(createdUser)).when(userService).getUserById(any(UUID.class));
        doNothing().when(authenticationService).addAuthorization(any(),any(), any());

        ErrorResponse errorResponse = given()
                .contentType(ContentType.JSON)
                .body(teacher)
                .put()
                .then()
                .statusCode(400)
                .extract().as(ErrorResponse.class);

        assertThat(errorResponse.getErrorId()).isNull();
        assertThat(errorResponse.getErrors())
                .isNotNull()
                .hasSize(2)
                .contains(
                        new ErrorResponse.ErrorMessage("createOrUpdate.guestTeacher.firstName", getErrorMessage("GuestTeacher.firstName.required")),
                        new ErrorResponse.ErrorMessage("createOrUpdate.guestTeacher.surname", getErrorMessage("GuestTeacher.surname.required"))
                );
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenCreateNewTeacherWithRequiredNameBlank_thenShouldReturnWithAnErrorResponse() {
        GuestTeacherPostResource teacher = getGuestTeacherPostResource();
        teacher.setFirstName("");

        User createdUser = createAndSaveUser();
        doReturn(Optional.ofNullable(createdUser)).when(userService).getUserById(any(UUID.class));
        doNothing().when(authenticationService).addAuthorization(any(),any(), any());

        ErrorResponse errorResponse = given()
                .contentType(ContentType.JSON)
                .body(teacher)
                .put()
                .then()
                .statusCode(400)
                .extract().as(ErrorResponse.class);

        assertThat(errorResponse.getErrorId()).isNull();
        assertThat(errorResponse.getErrors())
                .isNotNull()
                .hasSize(1)
                .contains(new ErrorResponse.ErrorMessage("createOrUpdate.guestTeacher.firstName", getErrorMessage("GuestTeacher.firstName.required")));
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenDeleteNonExistingTeacherById_thenShouldReturnUnauthorized() {
        given()
                .when()
                .delete("/{id}", UUID.randomUUID())
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    @Disabled("tokenHelper.getCustomClaim() returns null in AbstractSecurityInterceptor")
    void givenDeleteTeacherById_thenShouldDeleteSuccessfully() {
        GuestTeacherPostResource teacher = getGuestTeacherPostResource();
        GuestTeacher saved = saveTeacher(teacher);

        given()
                .contentType(ContentType.JSON)
                .delete("/{id}", saved.getId())
                .then()
                .statusCode(200);

        given()
                .contentType(ContentType.JSON)
                .get("/{id}", saved.getId())
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenUpdatingFirstNameOfTeacherById_thenShouldUpdateSuccessfully() {
        String newName = RandomStringUtils.randomAlphabetic(10);
        GuestTeacherPostResource teacher = getGuestTeacherPostResource();
        GuestTeacher saved = saveTeacher(teacher);

        saved.setFirstName(newName);
        assertNotNull(saved.getId());

        GuestTeacher updated = given()
                .contentType(ContentType.JSON)
                .body(saved)
                .put()
                .then()
                .statusCode(200)
                .extract().as(GuestTeacher.class);

        assertThat(updated.getFirstName()).isEqualTo(newName);
        assertThat(updated).isEqualTo(saved);
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenUpdatingUnknownTeacher_thenShouldReturnWithErrorResponse() {
        GuestTeacherPostResource guestTeacher = getGuestTeacherPostResource();
        guestTeacher.setId(UUID.randomUUID());

        given()
                .contentType(ContentType.JSON)
                .body(guestTeacher)
                .put()
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenUpdatingUnknownTeacherById_thenShouldReturnWithErrorResponse() {
        GuestTeacherPostResource guestTeacher = getGuestTeacherPostResource();
        guestTeacher.setId(UUID.randomUUID());

        given()
                .contentType(ContentType.JSON)
                .body(guestTeacher)
                .put()
                .then()
                .statusCode(404);
    }

    private GuestTeacher saveTeacher(GuestTeacherPostResource teacher) {
        User createdUser = createAndSaveUser();
        doReturn(Optional.ofNullable(createdUser)).when(userService).getUserById(any(UUID.class));

        Coordinates coordinates = Coordinates.builder()
                .lat(0.0)
                .lon(0.0)
                .build();
        doReturn(Optional.of(coordinates)).when(coordinatesService).getCoordinates(any(), any());

        doNothing().when(authenticationService).addAuthorization(any(),any(), any());

        return given()
                .contentType(ContentType.JSON)
                .body(teacher)
                .put()
                .then()
                .statusCode(200)
                .extract().as(GuestTeacher.class);
    }

    private GuestTeacherPostResource getGuestTeacherPostResource() {
        var teacher = new GuestTeacherPostResource();
        teacher.setFirstName(RandomStringUtils.randomAlphabetic(10));
        teacher.setSurname(RandomStringUtils.randomAlphabetic(10));
        teacher.setAddress(RandomStringUtils.randomAlphabetic(10));
        teacher.setPostalCode(RandomStringUtils.randomAlphabetic(10));
        teacher.setAvailability(EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY));

        return teacher;
    }

    private User createAndSaveUser() {
        String emailAdress = RandomStringUtils.randomAlphabetic(10) + "@littil.org";
        User user = new User();
        user.setEmailAddress(emailAdress);
        return userService.createUser(user);
    }
}
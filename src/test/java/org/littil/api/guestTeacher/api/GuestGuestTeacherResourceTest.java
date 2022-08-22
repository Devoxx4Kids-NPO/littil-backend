package org.littil.api.guestTeacher.api;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.littil.api.exception.ErrorResponse;
import org.littil.api.guestTeacher.service.GuestTeacher;

import java.time.DayOfWeek;
import java.util.EnumSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestHTTPEndpoint(GuestTeacherResource.class)
@Disabled("Disabled, needs a lot of refactoring")
class GuestGuestTeacherResourceTest {

    @Test
    void givenFindAll_thenShouldReturnMultipleTeachers() {
        given()
                .when()
                .get()
                .then()
                .statusCode(200);
    }

    @Test
    void givenGetTeacherById_thenShouldReturnSuccessfully() {
        GuestTeacher guestTeacher = createTeacher();
        GuestTeacher saved = given()
                .contentType(ContentType.JSON)
                .body(guestTeacher)
                .post()
                .then()
                .statusCode(201)
                .extract().as(GuestTeacher.class);

        GuestTeacher got = given()
                .when()
                .get("/{id}", saved.getId())
                .then()
                .statusCode(200)
                .extract().as(GuestTeacher.class);

        assertThat(saved).isEqualTo(got);
    }

    @Test
    void givenGetTeacherByUnknownId_thenShouldReturnNotFound() {
        given()
                .when()
                .get("/{id}", UUID.randomUUID())
                .then()
                .statusCode(404);
    }

    @Test
    void givenGetTeacherByName_thenShouldReturnSuccessfully() {
        String validSurname = RandomStringUtils.randomAlphabetic(10);
        GuestTeacher guestTeacher = createTeacher();
        guestTeacher.setSurname(validSurname);

        GuestTeacher saved = given()
                .contentType(ContentType.JSON)
                .body(guestTeacher)
                .post()
                .then()
                .statusCode(201)
                .extract().as(GuestTeacher.class);

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
    void givenGetTeacherByUnknownName_thenShouldReturnNotFound() {
        given()
                .when()
                .get("/{name}", RandomStringUtils.randomAlphabetic(10))
                .then()
                .statusCode(404);
    }

    @Test
    void givenCreateNewTeacher_thenShouldBeCreatedSuccessfully() {
        GuestTeacher guestTeacher = createTeacher();
        GuestTeacher saved = given()
                .contentType(ContentType.JSON)
                .body(guestTeacher)
                .post()
                .then()
                .statusCode(201)
                .extract().as(GuestTeacher.class);

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void givenCreateNewTeacherWithoutRequiredName_thenShouldReturnWithAnErrorResponse() {
        GuestTeacher guestTeacher = createTeacher();
        guestTeacher.setFirstName(null);

        ErrorResponse errorResponse = given()
                .contentType(ContentType.JSON)
                .body(guestTeacher)
                .post()
                .then()
                .statusCode(400)
                .extract().as(ErrorResponse.class);

        assertThat(errorResponse.getErrorId()).isNull();
        assertThat(errorResponse.getErrors())
                .isNotNull()
                .hasSize(1)
                .contains(new ErrorResponse.ErrorMessage("create.teacher.firstName", getErrorMessage("Teacher.firstName.required")));
    }

    @Test
    void givenCreateNewTeacherWithoutRequiredNamesAndInvalidEmail_thenShouldReturnWithAnErrorResponse() {
        GuestTeacher guestTeacher = createTeacher();
        guestTeacher.setSurname(null);
        guestTeacher.setFirstName(null);

        ErrorResponse errorResponse = given()
                .contentType(ContentType.JSON)
                .body(guestTeacher)
                .post()
                .then()
                .statusCode(400)
                .extract()
                .as(ErrorResponse.class);

        assertThat(errorResponse.getErrorId()).isNull();
        assertThat(errorResponse.getErrors())
                .isNotNull()
                .hasSize(3)
                .contains(
                        new ErrorResponse.ErrorMessage("create.teacher.firstName", getErrorMessage("Teacher.firstName.required")),
                        new ErrorResponse.ErrorMessage("create.teacher.surname", getErrorMessage("Teacher.surname.required"))
                );
    }

    @Test
    void givenDeleteNonExistingTeacherById_thenShouldReturnNotFound() {
        given()
                .contentType(ContentType.JSON)
                .delete("/{id}", UUID.randomUUID())
                .then()
                .statusCode(404);
    }

    @Test
    void givenDeleteTeacherById_thenShouldDeleteSuccessfully() {
        GuestTeacher guestTeacher = createTeacher();
        GuestTeacher saved = given()
                .contentType(ContentType.JSON)
                .body(guestTeacher)
                .post()
                .then()
                .statusCode(201)
                .extract().as(GuestTeacher.class);

        given()
                .contentType(ContentType.JSON)
                .delete("/{id}", saved.getId())
                .then()
                .statusCode(200);

        given()
                .contentType(ContentType.JSON)
                .get("/{id}", saved.getId())
                .then()
                .statusCode(404);
    }

    @Test
    void givenUpdatingFirstNameOfTeacherById_thenShouldUpdateSuccessfully() {
        GuestTeacher guestTeacher = createTeacher();
        String newName = RandomStringUtils.randomAlphabetic(10);

        GuestTeacher saved = given()
                .contentType(ContentType.JSON)
                .body(guestTeacher)
                .post()
                .then()
                .statusCode(201)
                .extract().as(GuestTeacher.class);

        saved.setFirstName(newName);

        GuestTeacher updated = given()
                .contentType(ContentType.JSON)
                .body(saved)
                .put("/{id}", saved.getId())
                .then()
                .statusCode(200)
                .extract().as(GuestTeacher.class);

        assertThat(updated.getFirstName()).isEqualTo(newName);
        assertThat(updated).isEqualTo(saved);
    }

    @Test
    void givenUpdatingIdOfTeacherById_thenShouldReturnWithErrorResponse() {
        GuestTeacher guestTeacher = createTeacher();
        UUID teacherId = UUID.randomUUID();
        UUID newTeacherId = UUID.randomUUID();

        guestTeacher.setId(newTeacherId);

        ErrorResponse errorResponse = given()
                .contentType(ContentType.JSON)
                .body(guestTeacher)
                .put("/{id}", teacherId)
                .then()
                .statusCode(500)
                .extract().as(ErrorResponse.class);

        assertThat(errorResponse.getErrors())
                .isNotNull()
                .hasSize(1)
                .contains(new ErrorResponse.ErrorMessage("Path variable id does not match Teacher.id"));
    }

    @Test
    void givenUpdatingUnknownTeacherById_thenShouldReturnWithErrorResponse() {
        GuestTeacher guestTeacher = createTeacher();
        guestTeacher.setId(UUID.randomUUID());

        given()
                .contentType(ContentType.JSON)
                .body(guestTeacher)
                .put("/{id}", guestTeacher.getId())
                .then()
                .statusCode(404);
    }

    @Test
    void givenUpdatingByIdWherePayloadIdDeviatesFromPathId_thenShouldReturnWithErrorResponse() {
        GuestTeacher guestTeacher = createTeacher();
        guestTeacher.setId(UUID.randomUUID());

        ErrorResponse errorResponse = given()
                .contentType(ContentType.JSON)
                .body(guestTeacher)
                .put("/{id}", UUID.randomUUID())
                .then()
                .statusCode(500)
                .extract().as(ErrorResponse.class);

        assertThat(errorResponse.getErrors())
                .isNotNull()
                .hasSize(1)
                .contains(new ErrorResponse.ErrorMessage("Path variable id does not match Teacher.id"));
    }

    @Test
    void givenTeacherByIdWithoutIdInPayload_thenShouldReturnWithErrorResponse() {
        GuestTeacher guestTeacher = createTeacher();

        ErrorResponse errorResponse = given()
                .contentType(ContentType.JSON)
                .body(guestTeacher)
                .put("/{id}", UUID.randomUUID())
                .then()
                .statusCode(500)
                .extract().as(ErrorResponse.class);

        assertThat(errorResponse.getErrors())
                .isNotNull()
                .hasSize(1)
                .contains(new ErrorResponse.ErrorMessage("Path variable id does not match Teacher.id"));
    }

    private GuestTeacher createTeacher() {
        GuestTeacher guestTeacher = new GuestTeacher();
        guestTeacher.setFirstName(RandomStringUtils.randomAlphabetic(10));
        guestTeacher.setSurname(RandomStringUtils.randomAlphabetic(10));
        guestTeacher.setPostalCode(RandomStringUtils.randomAlphabetic(10));
        guestTeacher.setLocale(RandomStringUtils.randomAlphabetic(2));
        guestTeacher.setAvailability(EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY));

        return guestTeacher;
    }

    private String getErrorMessage(String key) {
        return ResourceBundle.getBundle("ValidationMessages").getString(key);
    }
}

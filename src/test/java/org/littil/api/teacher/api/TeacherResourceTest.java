package org.littil.api.teacher.api;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.littil.api.exception.ErrorResponse;
import org.littil.api.teacher.service.Teacher;

import java.time.DayOfWeek;
import java.util.EnumSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestHTTPEndpoint(TeacherResource.class)
class TeacherResourceTest {

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
        Teacher teacher = createTeacher();
        Teacher saved = given()
                .contentType(ContentType.JSON)
                .body(teacher)
                .post()
                .then()
                .statusCode(201)
                .extract().as(Teacher.class);

        Teacher got = given()
                .when()
                .get("/{id}", saved.getId())
                .then()
                .statusCode(200)
                .extract().as(Teacher.class);

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
        Teacher teacher = createTeacher();
        teacher.setSurname(validSurname);

        Teacher saved = given()
                .contentType(ContentType.JSON)
                .body(teacher)
                .post()
                .then()
                .statusCode(201)
                .extract().as(Teacher.class);

        List<Teacher> got = given()
                .when()
                .get("/name/{name}", validSurname)
                .then()
                .statusCode(200)
                .extract()
                .jsonPath().getList(".", Teacher.class);

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
        Teacher teacher = createTeacher();
        Teacher saved = given()
                .contentType(ContentType.JSON)
                .body(teacher)
                .post()
                .then()
                .statusCode(201)
                .extract().as(Teacher.class);

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void givenCreateNewTeacherWithoutRequiredName_thenShouldReturnWithAnErrorResponse() {
        Teacher teacher = createTeacher();
        teacher.setFirstName(null);

        ErrorResponse errorResponse = given()
                .contentType(ContentType.JSON)
                .body(teacher)
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
        Teacher teacher = createTeacher();
        teacher.setEmail(RandomStringUtils.randomAlphabetic(10));
        teacher.setSurname(null);
        teacher.setFirstName(null);

        ErrorResponse errorResponse = given()
                .contentType(ContentType.JSON)
                .body(teacher)
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
                        new ErrorResponse.ErrorMessage("create.teacher.surname", getErrorMessage("Teacher.surname.required")),
                        new ErrorResponse.ErrorMessage("create.teacher.email", getErrorMessage("Teacher.email.invalid"))
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
        Teacher teacher = createTeacher();
        Teacher saved = given()
                .contentType(ContentType.JSON)
                .body(teacher)
                .post()
                .then()
                .statusCode(201)
                .extract().as(Teacher.class);

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
        Teacher teacher = createTeacher();
        String newName = RandomStringUtils.randomAlphabetic(10);

        Teacher saved = given()
                .contentType(ContentType.JSON)
                .body(teacher)
                .post()
                .then()
                .statusCode(201)
                .extract().as(Teacher.class);

        saved.setFirstName(newName);

        Teacher updated = given()
                .contentType(ContentType.JSON)
                .body(saved)
                .put("/{id}", saved.getId())
                .then()
                .statusCode(200)
                .extract().as(Teacher.class);

        assertThat(updated.getFirstName()).isEqualTo(newName);
        assertThat(updated).isEqualTo(saved);
    }

    @Test
    void givenUpdatingIdOfTeacherById_thenShouldReturnWithErrorResponse() {
        Teacher teacher = createTeacher();
        UUID teacherId = UUID.randomUUID();
        UUID newTeacherId = UUID.randomUUID();

        teacher.setId(newTeacherId);

        ErrorResponse errorResponse = given()
                .contentType(ContentType.JSON)
                .body(teacher)
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
        Teacher teacher = createTeacher();
        teacher.setId(UUID.randomUUID());

        given()
                .contentType(ContentType.JSON)
                .body(teacher)
                .put("/{id}", teacher.getId())
                .then()
                .statusCode(404);
    }

    @Test
    void givenUpdatingByIdWherePayloadIdDeviatesFromPathId_thenShouldReturnWithErrorResponse() {
        Teacher teacher = createTeacher();
        teacher.setId(UUID.randomUUID());

        ErrorResponse errorResponse = given()
                .contentType(ContentType.JSON)
                .body(teacher)
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
        Teacher teacher = createTeacher();

        ErrorResponse errorResponse = given()
                .contentType(ContentType.JSON)
                .body(teacher)
                .put("/{id}", UUID.randomUUID())
                .then()
                .statusCode(500)
                .extract().as(ErrorResponse.class);

        assertThat(errorResponse.getErrors())
                .isNotNull()
                .hasSize(1)
                .contains(new ErrorResponse.ErrorMessage("Path variable id does not match Teacher.id"));
    }

    private Teacher createTeacher() {
        Teacher teacher = new Teacher();
        teacher.setFirstName(RandomStringUtils.randomAlphabetic(10));
        teacher.setSurname(RandomStringUtils.randomAlphabetic(10));
        teacher.setEmail(RandomStringUtils.randomAlphabetic(10).concat("@littil.org"));
        teacher.setPostalCode(RandomStringUtils.randomAlphabetic(10));
        teacher.setLocale(RandomStringUtils.randomAlphabetic(2));
        teacher.setAvailability(EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY));

        return teacher;
    }

    private String getErrorMessage(String key) {
        return ResourceBundle.getBundle("ValidationMessages").getString(key);
    }
}

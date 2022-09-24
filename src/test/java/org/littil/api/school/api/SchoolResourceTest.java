package org.littil.api.school.api;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.restassured.http.ContentType;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.littil.api.auth.provider.Provider;
import org.littil.api.auth.service.AuthenticationService;
import org.littil.api.coordinates.service.Coordinates;
import org.littil.api.coordinates.service.CoordinatesService;
import org.littil.api.exception.ErrorResponse;
import org.littil.api.school.service.School;
import org.littil.api.user.repository.UserEntity;
import org.littil.api.user.repository.UserRepository;
import org.littil.api.user.service.User;
import org.littil.api.user.service.UserService;
import org.littil.mock.auth0.APIManagementMock;

import io.quarkus.test.security.oidc.Claim;

import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;

import javax.inject.Inject;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

@QuarkusTest
@TestHTTPEndpoint(SchoolResource.class)
@QuarkusTestResource(APIManagementMock.class)
class SchoolResourceTest {
    
    @InjectSpy
    UserService userService;
    
    @InjectMock
    CoordinatesService coordinatesService;
    
    @InjectMock
    AuthenticationService authenticationService;
    
    
    @Test
    void givenFindAllUnauthorized_thenShouldReturnForbidden() {
        given() //
                .when() //
                .get() //
                .then() //
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenFindAll_thenShouldReturnMultipleSchools() {
        given() //
                .when() //
                .get() //
                .then() //

                .statusCode(200);
    }
 

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenGetSchoolById_thenShouldReturnSuccessfully() {

        User user = new User();
        user.setEmailAddress("email@adres.nl");
        User createdUser = userService.createUser(user);
        
        doReturn(Optional.ofNullable(createdUser)).when(userService).getUserById(any(UUID.class)); 
        
        Coordinates coordinates = Coordinates.builder() //
                .lat(0.0) //
                .lon(0.0) //
                .build();
        doReturn(coordinates).when(coordinatesService).getCoordinates(any(), any());
        
        doNothing().when(authenticationService).addAuthorization(any(),any(), any());
          
        School school = createSchool();
        School saved = given()
                .contentType(ContentType.JSON)
                .body(school)
                .put()
                .then()
                .statusCode(200)
                .extract().as(School.class);
  
        School got = given()
                .when()
                .get("/{id}", saved.getId())
                .then()
                .statusCode(200)
                .extract().as(School.class);

        assertThat(saved).isEqualTo(got);
    }



//    @Test
//    void givenFindAll_thenShouldReturnMultipleSchools() {
//        given()
//                .when()
//                .get()
//                .then()
//                .statusCode(200);
//    }
//
//    @Test
//    void givenGetSchoolById_thenShouldReturnSuccessfully() {
//        School school = createSchool();
//        School saved = given()
//                .contentType(ContentType.JSON)
//                .body(school)
//                .post()
//                .then()
//                .statusCode(201)
//                .extract().as(School.class);
//
//        School got = given()
//                .when()
//                .get("/{id}", saved.getId())
//                .then()
//                .statusCode(200)
//                .extract().as(School.class);
//
//        assertThat(saved).isEqualTo(got);
//    }
//
//    @Test
//    void givenGetSchoolByUnknownId_thenShouldReturnNotFound() {
//        given()
//                .when()
//                .get("/{id}", UUID.randomUUID())
//                .then()
//                .statusCode(404);
//    }
//
//    @Test
//    void givenGetSchoolByName_thenShouldReturnSuccessfully() {
//        String validName = RandomStringUtils.randomAlphabetic(10);
//        School school = createSchool();
//        school.setName(validName);
//
//        School saved = given()
//                .contentType(ContentType.JSON)
//                .body(school)
//                .post()
//                .then()
//                .statusCode(201)
//                .extract().as(School.class);
//
//        List<School> got = given()
//                .when()
//                .get("/name/{name}", validName)
//                .then()
//                .statusCode(200)
//                .extract()
//                .jsonPath().getList(".", School.class);
//
//        assertThat(saved).isIn(got);
//    }
//
//    @Test
//    void givenGetSchoolByUnknownName_thenShouldReturnNotFound() {
//        given()
//                .when()
//                .get("/{name}", RandomStringUtils.randomAlphabetic(10))
//                .then()
//                .statusCode(404);
//    }
//
//    @Test
//    void givenCreateNewSchool_thenShouldBeCreatedSuccessfully() {
//        School school = createSchool();
//        School saved = given()
//                .contentType(ContentType.JSON)
//                .body(school)
//                .post()
//                .then()
//                .statusCode(201)
//                .extract().as(School.class);
//
//        assertThat(saved.getId()).isNotNull();
//    }
//
//    @Test
//    void givenCreateNewSchoolWithoutRequiredName_thenShouldReturnWithAnErrorResponse() {
//        School school = createSchool();
//        school.setName(null);
//
//        ErrorResponse errorResponse = given()
//                .contentType(ContentType.JSON)
//                .body(school)
//                .post()
//                .then()
//                .statusCode(400)
//                .extract().as(ErrorResponse.class);
//
//        assertThat(errorResponse.getErrorId()).isNull();
//        assertThat(errorResponse.getErrors())
//                .isNotNull()
//                .hasSize(1)
//                .contains(new ErrorResponse.ErrorMessage("create.school.name", getErrorMessage("School.name.required")));
//    }
//
//    @Test
//    void givenCreateNewSchoolWithRequiredNameBlank_thenShouldReturnWithAnErrorResponse() {
//        School school = createSchool();
//        school.setName("");
//
//        ErrorResponse errorResponse = given()
//                .contentType(ContentType.JSON)
//                .body(school)
//                .post()
//                .then()
//                .statusCode(400)
//                .extract().as(ErrorResponse.class);
//
//        assertThat(errorResponse.getErrorId()).isNull();
//        assertThat(errorResponse.getErrors())
//                .isNotNull()
//                .hasSize(1)
//                .contains(new ErrorResponse.ErrorMessage("create.school.name", getErrorMessage("School.name.required")));
//    }
//
//    @Test
//    void givenCreateNewSchoolWithoutRequiredNameAndAddressAndInvalidContactPersonEmail_thenShouldReturnWithAnErrorResponse() {
//        School school = createSchool();
//        school.setName(null);
//        school.setAddress(null);
//
//        ErrorResponse errorResponse = given()
//                .contentType(ContentType.JSON)
//                .body(school)
//                .post()
//                .then()
//                .statusCode(400)
//                .extract()
//                .as(ErrorResponse.class);
//
//        assertThat(errorResponse.getErrorId()).isNull();
//        assertThat(errorResponse.getErrors())
//                .isNotNull()
//                .hasSize(3)
//                .contains(
//                        new ErrorResponse.ErrorMessage("create.school.name", getErrorMessage("School.name.required")),
//                        new ErrorResponse.ErrorMessage("create.school.address", getErrorMessage("School.address.required")),
//                        new ErrorResponse.ErrorMessage("create.school.contactPersonEmail", getErrorMessage("School.contactPersonEmail.invalid"))
//                );
//    }
//
//    @Test
//    void givenDeleteNonExistingSchoolById_thenShouldReturnNotFound() {
//        given()
//                .contentType(ContentType.JSON)
//                .delete("/{id}", UUID.randomUUID())
//                .then()
//                .statusCode(404);
//    }
//
//    @Test
//    void givenDeleteSchoolById_thenShouldDeleteSuccessfully() {
//        School school = createSchool();
//        School saved = given()
//                .contentType(ContentType.JSON)
//                .body(school)
//                .post()
//                .then()
//                .statusCode(201)
//                .extract().as(School.class);
//
//        given()
//                .contentType(ContentType.JSON)
//                .delete("/{id}", saved.getId())
//                .then()
//                .statusCode(200);
//
//        given()
//                .contentType(ContentType.JSON)
//                .get("/{id}", saved.getId())
//                .then()
//                .statusCode(404);
//    }
//
//    @Test
//    void givenUpdatingNameOfSchoolById_thenShouldUpdateSuccessfully() {
//        School school = createSchool();
//        String newName = RandomStringUtils.randomAlphabetic(10);
//
//        School saved = given()
//                .contentType(ContentType.JSON)
//                .body(school)
//                .post()
//                .then()
//                .statusCode(201)
//                .extract().as(School.class);
//
//        saved.setName(newName);
//
//        School updated = given()
//                .contentType(ContentType.JSON)
//                .body(saved)
//                .put("/{id}", saved.getId())
//                .then()
//                .statusCode(200)
//                .extract().as(School.class);
//
//        assertThat(updated.getName()).isEqualTo(newName);
//        assertThat(updated).isEqualTo(saved);
//    }
//
//    @Test
//    void givenUpdatingIdOfSchoolById_thenShouldReturnWithErrorResponse() {
//        School school = createSchool();
//        UUID schoolId = UUID.randomUUID();
//        UUID newSchoolId = UUID.randomUUID();
//
//        school.setId(newSchoolId);
//
//        ErrorResponse errorResponse = given()
//                .contentType(ContentType.JSON)
//                .body(school)
//                .put("/{id}", schoolId)
//                .then()
//                .statusCode(500)
//                .extract().as(ErrorResponse.class);
//
//        assertThat(errorResponse.getErrors())
//                .isNotNull()
//                .hasSize(1)
//                .contains(new ErrorResponse.ErrorMessage("Path variable id does not match School.id"));
//    }
//
//    @Test
//    void givenUpdatingUnknownSchoolById_thenShouldReturnWithErrorResponse() {
//        School school = createSchool();
//        school.setId(UUID.randomUUID());
//
//        given()
//                .contentType(ContentType.JSON)
//                .body(school)
//                .put("/{id}", school.getId())
//                .then()
//                .statusCode(404);
//    }
//
//    @Test
//    void givenUpdatingByIdWherePayloadIdDeviatesFromPathId_thenShouldReturnWithErrorResponse() {
//        School school = createSchool();
//        school.setId(UUID.randomUUID());
//
//        ErrorResponse errorResponse = given()
//                .contentType(ContentType.JSON)
//                .body(school)
//                .put("/{id}", UUID.randomUUID())
//                .then()
//                .statusCode(500)
//                .extract().as(ErrorResponse.class);
//
//        assertThat(errorResponse.getErrors())
//                .isNotNull()
//                .hasSize(1)
//                .contains(new ErrorResponse.ErrorMessage("Path variable id does not match School.id"));
//    }
//
//    @Test
//    void givenSchoolByIdWithoutIdInPayload_thenShouldReturnWithErrorResponse() {
//        School school = createSchool();
//
//        ErrorResponse errorResponse = given()
//                .contentType(ContentType.JSON)
//                .body(school)
//                .put("/{id}", UUID.randomUUID())
//                .then()
//                .statusCode(500)
//                .extract().as(ErrorResponse.class);
//
//        assertThat(errorResponse.getErrors())
//                .isNotNull()
//                .hasSize(1)
//                .contains(new ErrorResponse.ErrorMessage("Path variable id does not match School.id"));
//    }

    private School createSchool() {
        School school = new School();
        school.setName(RandomStringUtils.randomAlphabetic(10));
        school.setAddress(RandomStringUtils.randomAlphabetic(10));
        school.setPostalCode(RandomStringUtils.randomAlphabetic(6));
        school.setFirstName(RandomStringUtils.randomAlphabetic(10));
        school.setSurname(RandomStringUtils.randomAlphabetic(10));

        return school;
    }

    private String getErrorMessage(String key) {
        return ResourceBundle.getBundle("ValidationMessages").getString(key);
    }
}

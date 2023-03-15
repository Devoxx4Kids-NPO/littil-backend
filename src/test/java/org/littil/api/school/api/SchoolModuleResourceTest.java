package org.littil.api.school.api;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.littil.api.auth.TokenHelper;
import org.littil.api.auth.authz.SchoolSecurityInterceptor;
import org.littil.api.auth.service.AuthenticationService;
import org.littil.api.module.service.Module;
import org.littil.api.module.service.ModuleService;
import org.littil.api.school.service.School;
import org.littil.api.school.service.SchoolService;
import org.littil.api.user.service.User;
import org.littil.api.user.service.UserService;
import org.littil.mock.auth0.APIManagementMock;

import javax.inject.Inject;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

@QuarkusTest
@TestHTTPEndpoint(SchoolModuleResource.class)
@QuarkusTestResource(APIManagementMock.class)
public class SchoolModuleResourceTest
{

    @InjectSpy
    UserService userService;
    @Inject
    SchoolService schoolService;
    @Inject
    ModuleService moduleService;
    @InjectMock
    TokenHelper tokenHelper;
    @InjectMock
    AuthenticationService authenticationService;
    @InjectMock(convertScopes=true)
    SchoolSecurityInterceptor schoolSecurityInterceptor;

    @BeforeEach
    void setup() throws IOException {
        doNothing().when(schoolSecurityInterceptor).filter(any());
    }
    @Test
    void givenFindSchoolModulesBySchoolId_Unauthorized_thenShouldReturnForbidden() {
        UUID schoolId = UUID.randomUUID();
        given()
                .when()
                .get("/{id}/modules", schoolId)
                .then() //
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenFindSchoolModulesBySchoolId_forUnknownSchoolId_thenShouldReturnNotFound() {
        UUID schoolId = UUID.randomUUID();
        given()
                .when()
                .get("/{id}/modules", schoolId)
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenFindSchoolModules_thenShouldReturnSchoolModules() {
        School school  = createAndSaveSchool();
        given()
                .when()
                .get("/{id}/modules", school.getId())
                .then()
                .statusCode(200);
    }

    @Test
    void givenDeleteSchoolModule_Unauthorized_thenShouldReturnForbidden() {
        given()
                .contentType(ContentType.JSON)
                .delete("/{school_id}/modules/{module_id}", UUID.randomUUID(), UUID.randomUUID())
                // school.getId(), module.getId())
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenDeleteSchoolModule_forUnkwnownSchoolId_thenShouldReturnNotFound() {
        given()
                .contentType(ContentType.JSON)
                .delete("/{school_id}/modules/{module_id}", UUID.randomUUID(), UUID.randomUUID())
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenDeleteSchoolModule_forUnknownSchoolModule_thenShouldReturnForbidden()  {
        School school  = createAndSaveSchool();

        given()
                .contentType(ContentType.JSON)
                .delete("/{school_id}/modules/{module_id}", school.getId(), UUID.randomUUID())
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenDeleteSchoolModule_thenShouldDeleteSchoolModule() {
        School school  = createAndSaveSchool();
        Module module = moduleService.findAll().get(0);

        given()
                .contentType(ContentType.JSON)
                .body(module)
                .post("/{id}/modules", school.getId())
                .then()
                .statusCode(201);

        given()
                .contentType(ContentType.JSON)
                .delete("/{school_id}/modules/{module_id}", school.getId(), module.getId())
                .then()
                .statusCode(200);
    }

    @Test
    void givenSaveSchoolModule_Unauthorized_thenShouldReturnForbidden() {
        School school  = createAndSaveSchool();
        Module module = moduleService.findAll().get(0);

        doReturn(true).when(tokenHelper).hasUserAuthorizations();

        given()
                .contentType(ContentType.JSON)
                .body(module)
                .post("/{id}/modules", school.getId())
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenSaveSchoolModule_thenShouldReturnSchoolModules() {
        School school  = createAndSaveSchool();
        Module module = moduleService.findAll().get(0);

        doReturn(true).when(tokenHelper).hasUserAuthorizations();

        given()
                .contentType(ContentType.JSON)
                .body(module)
                .post("/{id}/modules", school.getId())
                .then()
                .statusCode(201);
    }

    private School createAndSaveSchool()  {
        UUID userId = UUID.fromString("0ea41f01-cead-4309-871c-c029c1fe19bf");
        doReturn(userId).when(tokenHelper).getCurrentUserId();
        User createdUser = createAndSaveUser(userId);
        doReturn(Optional.ofNullable(createdUser)).when(userService).getUserById(any(UUID.class));
        doNothing().when(authenticationService).addAuthorization(any(), any(), any());
        School school = getDefaultSchool();
        return schoolService.saveOrUpdate(school, userId);
    }

    private User createAndSaveUser(UUID userId) {
        Optional<User> user = userService.getUserById(userId);
        if (user.isPresent()) {
            return user.get();
        }
            User newUser = new User();
            newUser.setId(userId);
            newUser.setEmailAddress(RandomStringUtils.randomAlphabetic(10) + "@littil.org");
            return userService.createUser(newUser);
    }

    private School getDefaultSchool() {
        School school = new School();
        school.setName(RandomStringUtils.randomAlphabetic(10));
        school.setAddress(RandomStringUtils.randomAlphabetic(10));
        school.setPostalCode(RandomStringUtils.randomAlphabetic(6));
        school.setFirstName(RandomStringUtils.randomAlphabetic(10));
        school.setSurname(RandomStringUtils.randomAlphabetic(10));
        return school;
    }
}
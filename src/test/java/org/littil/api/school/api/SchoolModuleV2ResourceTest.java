package org.littil.api.school.api;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.quarkus.test.junit.mockito.MockitoConfig;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.littil.TestFactory;
import org.littil.api.auth.TokenHelper;
import org.littil.api.auth.authz.SchoolSecurityInterceptor;
import org.littil.api.auth.service.AuthenticationService;
import org.littil.api.module.service.ModuleService;
import org.littil.api.school.service.School;
import org.littil.api.school.service.SchoolService;
import org.littil.api.user.service.User;
import org.littil.api.user.service.UserService;
import org.littil.mock.auth0.APIManagementMock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

@QuarkusTest
@TestHTTPEndpoint(SchoolModuleV2Resource.class)
@QuarkusTestResource(APIManagementMock.class)
public class SchoolModuleV2ResourceTest
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
    @InjectMock
    @MockitoConfig(convertScopes=true)
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
    void givenSaveSchoolModules_Unauthorized_thenShouldReturnForbidden() {
        School school  = createAndSaveSchool();
        List<String> modules = new ArrayList<>();

        doReturn(true).when(tokenHelper).hasUserAuthorizations();

        given()
                .contentType(ContentType.JSON)
                .body(modules)
                .post("/{id}/modules", school.getId())
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenSaveSchoolModules_thenShouldReturnOK() {
        School school  = createAndSaveSchool();
        List<String> modules = List.of(moduleService.findAll().get(0).getId().toString());

        doReturn(true).when(tokenHelper).hasUserAuthorizations();

        given()
                .contentType(ContentType.JSON)
                .body(modules)
                .post("/{id}/modules", school.getId())
                .then()
                .statusCode(200);
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
        return userService.getUserById(userId)
                .orElseGet(() -> userService.createUser(TestFactory.createUser(userId)));

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
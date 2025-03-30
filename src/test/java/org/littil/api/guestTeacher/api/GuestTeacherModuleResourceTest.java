package org.littil.api.guestTeacher.api;

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
import org.littil.RandomStringGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.littil.TestFactory;
import org.littil.api.auth.TokenHelper;
import org.littil.api.auth.authz.GuestTeacherSecurityInterceptor;
import org.littil.api.auth.service.AuthenticationService;
import org.littil.api.guestTeacher.service.GuestTeacher;
import org.littil.api.guestTeacher.service.GuestTeacherService;
import org.littil.api.module.service.Module;
import org.littil.api.module.service.ModuleService;
import org.littil.api.user.service.User;
import org.littil.api.user.service.UserService;
import org.littil.mock.auth0.APIManagementMock;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

@QuarkusTest
@TestHTTPEndpoint(GuestTeacherModuleResource.class)
@QuarkusTestResource(APIManagementMock.class)
class GuestTeacherModuleResourceTest
{

    @InjectSpy
    UserService userService;
    @Inject
    GuestTeacherService guestTeacherService;
    @Inject
    ModuleService moduleService;
    @InjectMock
    TokenHelper tokenHelper;
    @InjectMock
    AuthenticationService authenticationService;
    @InjectMock
    @MockitoConfig(convertScopes=true)
    GuestTeacherSecurityInterceptor guestTeacherSecurityInterceptor;

    @BeforeEach
    void setup() throws IOException {
        doNothing().when(guestTeacherSecurityInterceptor).filter(any());
    }
    @Test
    void givenFindGuestTeacherModulesByGuestTeacherId_Unauthorized_thenShouldReturnForbidden() {
        UUID guestTeacherId = UUID.randomUUID();
        given()
                .when()
                .get("/{id}/modules", guestTeacherId)
                .then() //
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenFindGuestTeacherModulesByGuestTeacherId_forUnknownGuestTeacherId_thenShouldReturnNotFound() {
        UUID guestTeacherId = UUID.randomUUID();
        given()
                .when()
                .get("/{id}/modules", guestTeacherId)
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenFindGuestTeacherModules_thenShouldReturnGuestTeacherModules() {
        GuestTeacher guestTeacher  = createAndSaveGuestTeacher();
        given()
                .when()
                .get("/{id}/modules", guestTeacher.getId())
                .then()
                .statusCode(200);
    }

    @Test
    void givenSaveGuestTeacherModule_Unauthorized_thenShouldReturnForbidden() {
        GuestTeacher guestTeacher  = createAndSaveGuestTeacher();
        Module module = moduleService.findAll().get(0);

        doReturn(true).when(tokenHelper).hasUserAuthorizations();

        given()
                .contentType(ContentType.JSON)
                .body(module)
                .post("/{id}/modules", guestTeacher.getId())
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenSaveGuestTeacherModule_thenShouldReturnHttpStatusOK() {
        GuestTeacher guestTeacher  = createAndSaveGuestTeacher();
        List<String> modules = List.of(moduleService.findAll().get(0).getId().toString());

        doReturn(true).when(tokenHelper).hasUserAuthorizations();

        given()
                .contentType(ContentType.JSON)
                .body(modules)
                .post("/{id}/modules", guestTeacher.getId())
                .then()
                .statusCode(200);
    }

    private GuestTeacher createAndSaveGuestTeacher()  {
        UUID userId = UUID.fromString("0ea41f01-cead-4309-871c-c029c1fe19bf");
        doReturn(userId).when(tokenHelper).getCurrentUserId();
        User createdUser = createAndSaveUser(userId);
        doReturn(Optional.ofNullable(createdUser)).when(userService).getUserById(any(UUID.class));
        doNothing().when(authenticationService).addAuthorization(any(), any(), any());
        GuestTeacher guestTeacher = getDefaultGuestTeacher();
        return guestTeacherService.saveOrUpdate(guestTeacher, userId);
    }

    private User createAndSaveUser(UUID userId) {
        Optional<User> user = userService.getUserById(userId);
        return user.orElseGet(() -> userService.createUser(TestFactory.createUser(userId)));
    }

    private GuestTeacher getDefaultGuestTeacher() {
        GuestTeacher guestTeacher = new GuestTeacher();
        guestTeacher.setAddress(RandomStringGenerator.generate(10));
        guestTeacher.setPostalCode(RandomStringGenerator.generate(6));
        guestTeacher.setFirstName(RandomStringGenerator.generate(10));
        guestTeacher.setSurname(RandomStringGenerator.generate(10));
        return guestTeacher;
    }
}
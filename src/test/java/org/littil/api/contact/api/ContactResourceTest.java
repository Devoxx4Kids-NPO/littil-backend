package org.littil.api.contact.api;

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
import org.junit.jupiter.api.Test;
import org.littil.api.auth.TokenHelper;
import org.littil.api.auth.service.AuthenticationService;
import org.littil.api.contact.service.Contact;
import org.littil.api.exception.ErrorResponse;
import org.littil.api.school.api.SchoolPostResource;
import org.littil.api.school.api.SchoolResource;
import org.littil.api.school.repository.SchoolEntity;
import org.littil.api.school.repository.SchoolRepository;
import org.littil.api.school.service.School;
import org.littil.api.user.service.User;
import org.littil.api.user.service.UserService;
import org.littil.mock.auth0.APIManagementMock;
import org.littil.mock.coordinates.service.WireMockSearchService;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.littil.Helper.withSchoolAuthorization;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

@QuarkusTest
@TestHTTPEndpoint(ContactResource.class)
@QuarkusTestResource(APIManagementMock.class)
@QuarkusTestResource(WireMockSearchService.class)
class ContactResourceTest {
    
    @InjectSpy
    UserService userService;
    @InjectMock
    AuthenticationService authenticationService;
    @InjectMock
    TokenHelper tokenHelper;

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

    /*
    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenGetContactById_thenShouldReturnSuccessfully() {
        ContactPostResource contact = getDefaultContact();
        Contact saved = save(contact);
        Contact got = given()
                .when()
                .get("/{id}", saved.getId())
                .then()
                .statusCode(200)
                .extract()
                .as(Contact.class);

        assertThat(saved).isEqualTo(got);
    }*/

    private Contact save(ContactPostResource contact) {
        doReturn(UUID.fromString("0ea41f01-cead-4309-871c-c029c1fe19bf")).when(tokenHelper).getCurrentUserId();

        User createdUser = createAndSaveUser();
        doReturn(Optional.ofNullable(createdUser))
                .when(userService).getUserById(any(UUID.class));
        doNothing().when(authenticationService).addAuthorization(any(),any(), any());

        return given()
                .contentType(ContentType.JSON)
                .body(contact)
                .post()
                .then()
                .statusCode(200)
                .extract().as(Contact.class);
    }

    private static ContactPostResource getDefaultContact() {
        ContactPostResource contact = new ContactPostResource();
        contact.setMedium("+31 6 1337 1337");
        contact.setMessage("Hello world");
        return contact;
    }

    private User createAndSaveUser() {
        String emailAdress = RandomStringUtils.randomAlphabetic(10) + "@littil.org";
        User user = new User();
        user.setEmailAddress(emailAdress);
        return userService.createUser(user);
    }
}

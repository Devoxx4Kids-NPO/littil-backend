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
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.littil.TestFactory;
import org.littil.api.auth.TokenHelper;
import org.littil.api.auth.service.AuthenticationService;
import org.littil.api.user.service.User;
import org.littil.api.user.service.UserService;
import org.littil.mock.auth0.APIManagementMock;
import org.littil.mock.coordinates.service.WireMockSearchService;

import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
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
    TokenHelper tokenHelper;
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
    void givenFindAll_thenShouldReturnOk() {
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
    void givenCreateNewContact_thenShouldReturnCreatedSuccessfully() {
        var contact = getContactPostResource(UUID.fromString("0ea41f01-cead-4309-871c-c029c1fe19bf"),"michael@littil.org","Berichtje");
        var saved = sendAndSave(contact);

        assertThat(saved.getStatusCode()).isEqualTo(200);
    }

    private Response sendAndSave(ContactPostResource contact) {
        doReturn(UUID.fromString("0ea41f01-cead-4309-871c-c029c1fe19bf"))
                .when(tokenHelper).getCurrentUserId();
        User createdUser = createAndSaveUser();
        doReturn(Optional.ofNullable(createdUser))
                .when(userService).getUserById(any(UUID.class));

        doNothing().when(authenticationService).addAuthorization(any(), any(), any());

        return given()
                .contentType(ContentType.JSON)
                .body(contact)
                .post()
                .then()
                .statusCode(200)
                .extract()
                .response();
    }

    private ContactPostResource getContactPostResource(UUID recipient,String medium, String message) {
        var contact = new ContactPostResource();
        contact.setRecipient(recipient);
        contact.setMedium(medium);
        contact.setMessage(message);
        return contact;
    }

    private User createAndSaveUser() {
        return userService.createUser(TestFactory.createUser());
    }
}
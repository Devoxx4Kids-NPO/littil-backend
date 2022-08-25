package org.littil.api.auth;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import org.junit.jupiter.api.Test;
import org.littil.api.userSetting.api.UserSettingResource;
import org.littil.mock.auth0.OidcServerMock;

import static io.restassured.RestAssured.given;

@QuarkusTest
@QuarkusTestResource(OidcServerMock.class)
public class BearerTokenAuthorizationTest {

    @Test
    @TestHTTPEndpoint(UserSettingResource.class)
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf")
    })
    public void whenGetAllUserSettingWithoutValidToken_thenReturnAllUserSpecificSettings() {
        given()
                .when()
                .then()
                .statusCode(200);
    }

    @Test
    @TestHTTPEndpoint(UserSettingResource.class)
    @TestSecurity(user = "littil", roles = "viewer")
    public void whenGetAllUserSettingWithoutLittilClaim_thenForbiddenToAccessResource() {
        given()
                .when()
                .then()
                .statusCode(403);
    }
}

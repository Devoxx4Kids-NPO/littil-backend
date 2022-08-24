package org.littil.api.userSetting.api;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestHTTPEndpoint(UserSettingResource.class)
class UserSettingResourceTest {

    @Test
    void givenListAllUnauthorized_thenShouldReturnForbidden() {
        given()
                .when()
                .get()
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @JwtSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf")
    })
    @Disabled("Needs debugging")
    void givenListAllAuthenticated_thenShouldReturnOk() {
        given()
                .when()
                .get()
                .then()
                .statusCode(200);
    }
}
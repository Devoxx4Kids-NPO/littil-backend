package org.littil.api.userSetting.api;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
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
}
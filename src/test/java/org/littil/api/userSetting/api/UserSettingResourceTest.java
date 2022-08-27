package org.littil.api.userSetting.api;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.restassured.http.ContentType;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.littil.api.exception.ErrorResponse;
import org.littil.api.userSetting.service.UserSetting;
import org.littil.mock.auth0.APIManagementMock;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.littil.Helper.getErrorMessage;

@QuarkusTest
@TestHTTPEndpoint(UserSettingResource.class)
@QuarkusTestResource(APIManagementMock.class)
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
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf")
    })
    void givenListAllAuthenticated_thenShouldReturnOk() {
        given()
                .when()
                .get()
                .then()
                .statusCode(200);
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf")
    })
    void givenWhenCreatingUserSetting_thenShouldReturnOk() {
        UserSetting userSetting = createUserSetting();

        UserSetting saved = given()
                .contentType(ContentType.JSON)
                .body(userSetting)
                .post()
                .then()
                .statusCode(201)
                .extract().as(UserSetting.class);

        assertThat(saved.getKey()).isNotNull();
    }

    @Test
    void givenWhenCreatingUserSettingUnauthorized_thenShouldReturnUnauthorized() {
        UserSetting userSetting = createUserSetting();

        given()
                .contentType(ContentType.JSON)
                .body(userSetting)
                .post()
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    void givenWhenCreatingUserSettingWithoutCustomLittilClaim_thenShouldReturnForbidden() {
        UserSetting userSetting = createUserSetting();

        given()
                .contentType(ContentType.JSON)
                .body(userSetting)
                .post()
                .then()
                .statusCode(403);
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf")
    })
    void givenWhenCreatingUserSettingWithoutValue_thenShouldReturnErrorResponse() {
        UserSetting userSetting = createUserSetting();
        userSetting.setValue(null);

        ErrorResponse errorResponse = given()
                .contentType(ContentType.JSON)
                .body(userSetting)
                .post()
                .then()
                .statusCode(400)
                .extract().as(ErrorResponse.class);

        assertThat(errorResponse.getErrorId()).isNull();
        assertThat(errorResponse.getErrors())
                .isNotNull()
                .hasSize(1)
                .contains(new ErrorResponse.ErrorMessage("create.userSetting.value", getErrorMessage("UserSetting.value.required")));
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf")
    })
    void givenWhenCreatingUserSettingWithoutKey_thenShouldReturnErrorResponse() {
        UserSetting userSetting = createUserSetting();
        userSetting.setKey(null);

        ErrorResponse errorResponse = given()
                .contentType(ContentType.JSON)
                .body(userSetting)
                .post()
                .then()
                .statusCode(400)
                .extract().as(ErrorResponse.class);

        assertThat(errorResponse.getErrorId()).isNull();
        assertThat(errorResponse.getErrors())
                .isNotNull()
                .hasSize(1)
                .contains(new ErrorResponse.ErrorMessage("create.userSetting.key", getErrorMessage("UserSetting.key.required")));
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf")
    })
    void givenGetByKey_thenShouldReturnUserSetting() {
        String key = RandomStringUtils.randomAlphabetic(5);
        UserSetting userSetting = createUserSetting();
        userSetting.setKey(key);

        UserSetting saved = given()
                .contentType(ContentType.JSON)
                .body(userSetting)
                .post()
                .then()
                .statusCode(201)
                .extract().as(UserSetting.class);

        UserSetting got = given()
                .when()
                .get("/{key}", key)
                .then()
                .statusCode(200)
                .extract()
                .as(UserSetting.class);

        assertThat(saved).isEqualTo(got);
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf")
    })
    void givenUpdate_thenShouldUpdateAndReturnUpdatedDto() {
        String key = RandomStringUtils.randomAlphabetic(5);
        String newValue = RandomStringUtils.randomAlphabetic(10);
        UserSetting userSetting = createUserSetting();
        userSetting.setKey(key);

        UserSetting saved = given()
                .contentType(ContentType.JSON)
                .body(userSetting)
                .post()
                .then()
                .statusCode(201)
                .extract()
                .as(UserSetting.class);

        userSetting.setValue(newValue);

        UserSetting updatedDto = given()
                .contentType(ContentType.JSON)
                .when()
                .body(userSetting)
                .put("/{key}", key)
                .then()
                .statusCode(200)
                .extract()
                .as(UserSetting.class);

        assertThat(saved.getKey()).isEqualTo(updatedDto.getKey());
        assertThat(updatedDto.getValue()).isEqualTo(newValue);
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf")
    })
    void givenUpdateWithIncorrectKeyInPath_thenShouldReturnErrorResponseAndInternalServerError() {
        UserSetting userSetting = createUserSetting();

        ErrorResponse errorResponse = given()
                .contentType(ContentType.JSON)
                .when()
                .body(userSetting)
                .put("/{key}", RandomStringUtils.randomAlphabetic(5))
                .then()
                .statusCode(500)
                .extract().as(ErrorResponse.class);

        assertThat(errorResponse.getErrorId()).isNull();
        assertThat(errorResponse.getErrors())
                .isNotNull()
                .hasSize(1)
                .contains(new ErrorResponse.ErrorMessage("Path variable key does not match UserSetting.key"));
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf")
    })
    void givenUpdateOfUnknownUserSetting_thenShouldReturnNotFound() {
        String key = RandomStringUtils.randomAlphabetic(5);
        UserSetting userSetting = createUserSetting();
        userSetting.setKey(key);

        given()
                .contentType(ContentType.JSON)
                .when()
                .body(userSetting)
                .put("/{key}", key)
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf")
    })
    void givenUpdateWithInvalidUserSettingDto_thenShouldReturnErrorResponse() {
        String key = RandomStringUtils.randomAlphabetic(5);
        UserSetting userSetting = createUserSetting();
        userSetting.setKey(key);

        given()
                .contentType(ContentType.JSON)
                .body(userSetting)
                .post()
                .then()
                .statusCode(201)
                .extract()
                .as(UserSetting.class);

        userSetting.setValue(null);

        ErrorResponse errorResponse = given()
                .contentType(ContentType.JSON)
                .when()
                .body(userSetting)
                .put("/{key}", key)
                .then()
                .statusCode(400)
                .extract().as(ErrorResponse.class);

        assertThat(errorResponse.getErrorId()).isNull();
        assertThat(errorResponse.getErrors())
                .isNotNull()
                .hasSize(1)
                .contains(new ErrorResponse.ErrorMessage("update.userSetting.value", getErrorMessage("UserSetting.value.required")));
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf")
    })
    void givenDeleteOfExistingUserSetting_thenShouldDeleteAndRespondOk() {
        String key = RandomStringUtils.randomAlphabetic(5);
        UserSetting userSetting = createUserSetting();
        userSetting.setKey(key);

        given()
                .contentType(ContentType.JSON)
                .body(userSetting)
                .post()
                .then()
                .statusCode(201)
                .extract()
                .as(UserSetting.class);

        given()
                .contentType(ContentType.JSON)
                .delete("/{key}", key)
                .then()
                .statusCode(200);

        given()
                .contentType(ContentType.JSON)
                .get("/{key}", key)
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf")
    })
    void givenDeleteOfUnknownUserSetting_thenShouldReponseNotFound() {
        given()
                .contentType(ContentType.JSON)
                .delete("/{key}", RandomStringUtils.randomAlphabetic(5))
                .then()
                .statusCode(404);
    }

    private UserSetting createUserSetting() {
        UserSetting userSetting = new UserSetting();
        userSetting.setKey(RandomStringUtils.randomAlphabetic(5));
        userSetting.setValue(RandomStringUtils.randomAlphabetic(10));
        return userSetting;
    }
}
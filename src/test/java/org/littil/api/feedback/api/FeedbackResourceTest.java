package org.littil.api.feedback.api;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestHTTPEndpoint(FeedbackResource.class)
class FeedbackResourceTest {

    @Test
    void givenFeedback_thenShouldReturnOK() {
        FeedbackPostResource feedback = new FeedbackPostResource();
        feedback.setFeedbackType("type");
        feedback.setMessage("message");
        feedback.setTimestamp(System.currentTimeMillis());

        given()
                .contentType(ContentType.JSON)
                .body(feedback)
                .post()
                .then()
                .statusCode(200);
    }

}
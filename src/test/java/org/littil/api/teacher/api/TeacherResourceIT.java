package org.littil.api.teacher.api;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@QuarkusTest
@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestTransaction
@Disabled("Disabled until we have integration tests")
class TeacherResourceIT {

    private static final String INITIAL_PAYLOAD = "{\"firstName\":\"John\",\"surname\":\"Doe\",\"email\":\"john@me.com\",\"postalCode\":\"1234VH\",\"country\":\"NL\",\"availability\":[\"MONDAY\",\"WEDNESDAY\"]}";

    @BeforeAll
    void setup() {
        RestAssured.basePath = "/api/v1/teacher";
    }

    @Test
    @Order(1)
    void create() {
        Response r = given().log().all()
                .header("Content-Type", "application/json")
                .and()
                .body(INITIAL_PAYLOAD)
                .when()
                .post()
                .then()
                .extract().response();

        assertEquals(javax.ws.rs.core.Response.Status.CREATED.getStatusCode(), r.statusCode());
        assertNotNull(r.jsonPath().getUUID("id"));
        assertEquals("John", r.jsonPath().getString("firstName"));
        assertEquals("Doe", r.jsonPath().getString("surname"));
        assertEquals("john@me.com", r.jsonPath().getString("email"));
        assertEquals("1234VH", r.jsonPath().getString("postalCode"));
        assertEquals("NL", r.jsonPath().getString("country"));
        assertNull(r.jsonPath().get("preferences"));
//        assertEquals(Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), r.jsonPath().getList("availability"));
    }
}
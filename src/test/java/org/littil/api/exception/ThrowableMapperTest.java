package org.littil.api.exception;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.littil.api.guestTeacher.api.GuestTeacherResource;
import org.littil.api.guestTeacher.service.GuestTeacherService;
import org.mockito.Mockito;

import java.util.ResourceBundle;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestHTTPEndpoint(GuestTeacherResource.class)
@Disabled("Disabled, needs a lot of refactoring")
class ThrowableMapperTest {

    @InjectMock
    GuestTeacherService teacherService;

    @Test
    void throwUnexpectedRuntimeException() {
        Mockito.when(teacherService.findAll()).thenThrow(new RuntimeException("Completely Unexpected"));
        ErrorResponse errorResponse = given()
                .when()
                .get()
                .then()
                .statusCode(500)
                .extract().as(ErrorResponse.class);
        assertThat(errorResponse.getErrorId()).isNotNull();
        assertThat(errorResponse.getErrors())
                .isNotNull()
                .hasSize(1)
                .contains(new ErrorResponse.ErrorMessage(ResourceBundle.getBundle("ValidationMessages").getString("System.error")));
    }
}
package org.littil.api.exception;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.junit.jupiter.api.Test;
import org.littil.api.teacher.api.TeacherResource;
import org.littil.api.teacher.service.TeacherService;
import org.mockito.Mockito;

import java.util.ResourceBundle;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestHTTPEndpoint(TeacherResource.class)
class ThrowableMapperTest {

    @InjectMock
    TeacherService teacherservice;

    @Test
    public void throwUnexpectedRuntimeException() {
        Mockito.when(teacherservice.findAll()).thenThrow(new RuntimeException("Completely Unexpected"));
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
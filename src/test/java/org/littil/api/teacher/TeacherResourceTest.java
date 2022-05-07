package org.littil.api.teacher;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;

import java.net.URL;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
class TeacherResourceTest {
	
	@TestHTTPEndpoint(TeacherResource.class)
	@TestHTTPResource("")
	URL teacherResourceEndpoint;
	
	@Test
	@Order(1)
	void whenGetListOfTeacher_thenResponseIsempty() {
		given()
			.when()
				.get(teacherResourceEndpoint)
			.then()
            	.statusCode(200)
            	.contentType(ContentType.JSON)
            	.body("size()", is(0));    
	}
	
	@Test
	@Order(2)
	void whenAddNewTeacher_thenResponseContainsTeacher() {
		 TeacherDto requestBody = new TeacherDto(1L, "firstName", "surName", "email@info.nl", "1234AB", "NL", null, null);
	     given()
	     		.contentType(ContentType.JSON)
	     		.body(requestBody)
	     	.when()
	            .post(teacherResourceEndpoint)
	        .then()
	        	.statusCode(200)
	        	.body("size()",is(1));
	}
	
	@Test
	@Order(3)
	void whenGetExistingTeacherById_thenResponseContainsTeacher() {
	    TeacherDto response[]  = given() 
	    	.when()  
	            .get(teacherResourceEndpoint)
	        .then()
	            .statusCode(200)
	        .extract()
	            .as(TeacherDto[].class);

	    assertEquals(1, response.length);
	    Long teacherDtoId = response[0].getId();
		
		given() 
			.when() 
				.get(teacherResourceEndpoint + "/" + teacherDtoId) 
			.then() 
				.statusCode(200) 
				.body("firstName", is( "firstName" ));
   }
		
	@Test
	@Order(4)
	void whenGetExistingTeacherBySurname_thenResponseContainsTeacher() {
		given() 
			.when() 
				.get(teacherResourceEndpoint + "/surname/surName")
			.then()
				.statusCode(200)
				.body("size()", is(1));	    
	}
			
	@Test
	@Order(5)
	void whenDeleteTeacher_thenResponseIsEmpty() {
	    TeacherDto response[]  = given() 
            .when()  
	            .get(teacherResourceEndpoint)
            .then()
	            .statusCode(200)
            .extract()
	            .as(TeacherDto[].class);
	    
	    assertEquals(1, response.length);
	    Long teacherDtoId = response[0].getId();

		given() 
           	.when() 
				.delete(teacherResourceEndpoint + "/" + teacherDtoId)
			.then()
				.statusCode(200)
				.body("size()", is(0));
	}

}

package org.littil.api.school;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;

import java.net.URL;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
class SchoolResourceTest {
		
	@TestHTTPEndpoint(SchoolResource.class)
	@TestHTTPResource("")
	URL schoolResourceEndpoint;
		
	@Test
	@Order(1)
	void whenGetListOfSchools_thenResponseIsempty() {
		given()
			.when()
         		.get(schoolResourceEndpoint)
         	.then()
         		.statusCode(200)
         		.contentType(ContentType.JSON)
         		.body("size()", is(0));    
	}

	@Test
	@Order(2)
	void whenAddNewSchool_thenResponseContainsSchool() {
		 SchoolDto requestBody = new SchoolDto(0L, "name", "adres", "postalCode", "contactPersonName", "contactPersonEmail");
	     given()
	     	.contentType(ContentType.JSON)
	        .body(requestBody)
	     .when()
	      	.post(schoolResourceEndpoint)
	     .then()
	     	.statusCode(200)
	     	.body("size()", is(1));
    }
	
	@Test
	@Order(3)
	void whenGetExistingSchoolById_thenResponseContainsSchool() {
	    SchoolDto response[]  = given() 
	        .when()  
	            .get(schoolResourceEndpoint)
	        .then()
	            .statusCode(200)
	        .extract()
	            .as(SchoolDto[].class);

	    assertEquals(1, response.length);
	    Long schoolDtoId = response[0].getId();
		
		given() 
			.when() 
				.get(schoolResourceEndpoint + "/" + schoolDtoId)
			.then()
				.statusCode(200)
				.body("name", is( "name" ));
   }
	
	@Test
	@Order(4)
	void whenGetExistingSchoolByName_thenResponseContainsSchool() {
		given() 
		    .when() 
				.get(schoolResourceEndpoint + "/name/name") 
		    .then()  
		    	.statusCode(200) 
		    	.body("size()", is(1));    
   }

	
	@Test
	@Order(5)
	void whenDeleteExistingSchool_thenResponseIsEmtpy() {
		SchoolDto response[]  = given() 
            .when()  
            	.get(schoolResourceEndpoint)
            .then()
            	.statusCode(200)
            .extract()
            	.as(SchoolDto[].class);
    
		assertEquals(1, response.length);
		SchoolDto requestBody = response[0];

		given()
    		.when()
    			.delete(schoolResourceEndpoint + "/" + requestBody.getId()) 
    		.then() 
				.statusCode(200) 
				.body("size()", is(0));    
	}

}

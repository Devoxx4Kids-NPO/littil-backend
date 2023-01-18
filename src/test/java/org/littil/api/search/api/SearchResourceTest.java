package org.littil.api.search.api;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.littil.api.search.service.SearchResult;
import org.littil.api.search.service.SearchService;
import org.littil.mock.auth0.APIManagementMock;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;

@QuarkusTest
@TestHTTPEndpoint(SearchResource.class)
@QuarkusTestResource(APIManagementMock.class)
class SearchResourceTest {
    
    @InjectSpy
    SearchService searchService;
    
     @Test
    void givenGetUnauthorized_thenShouldReturnForbidden() {
        given() //
                .when() //
                .queryParam("lat", 0.0)
                .queryParam("lon", 0.0)
                .queryParam("userType", UserType.SCHOOL.getLabel())
                .get() //
                .then() //
                .statusCode(401);
    }
     
     @Test
     @TestSecurity(user = "littil", roles = "viewer")
     @OidcSecurity(claims = {
             @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
     void givenGetAuthorzed_thenShouldReturnList() {
        
        doReturn(new ArrayList<>()).when(searchService).getSearchResults(anyDouble(), anyDouble(), any(UserType.class) );
        List<SearchResult> result = given() //
                 .when() //
                 .queryParam("lat", 0.0)
                 .queryParam("lon", 0.0)
                 .queryParam("userType", UserType.SCHOOL.getLabel())
                 .get() //
                 .then() //
                 .statusCode(200)
                 .extract()
                 .jsonPath().getList(".", SearchResult.class);
        assertNotNull(result);
        assertTrue(result.isEmpty());
     }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf") })
    void givenGetAuthorzed_withEmptyUserType_thenShouldReturnAllTypes() {

        given() //
                .when() //
                .queryParam("lat", 0.0)
                .queryParam("lon", 0.0)
                .queryParam("userType", "")
                .get() //
                .then() //
                .statusCode(200)
                .extract()
                .jsonPath().getList(".", SearchResult.class);
    }
}

package org.littil.api.coordinates.service;

import static org.junit.jupiter.api.Assertions.*;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.littil.mock.coordinates.service.WireMockSearchService;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(WireMockSearchService.class)
class CoordinatesServiceTest {
    
    private static final String NOT_FOUND = WireMockSearchService.NOT_FOUND;
    
    @Inject
    CoordinatesService service;
    
    @Test 
    void givenGetCoordinates__thenShouldReturnCoordinates() {
        Coordinates coordinates = service.getCoordinates("1234AB", "Address");
        assertNotNull(coordinates);
    }
 
    @Test 
    void givenGetCoordinatesNotFound__thenShouldReturnIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> service.getCoordinates("1234AB" , NOT_FOUND));
    }
    
    @Test
    void givenGetCoordinatesWithNullPostalCode__thenShouldReturnNullPointerException() {
        assertThrows(NullPointerException.class, () -> service.getCoordinates(null , "adress"));
    }

    @Test
    void givenGetCoordinatesWithBlankPostalCode__thenShouldReturnIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> service.getCoordinates("" , "adress"));
    }

    @Test
    void givenGetCoordinatesWithBlankAddress__thenShouldReturnIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> service.getCoordinates("1234AB" , ""));
    }

    @Test
    void givenGetCoordinatesWithNullAddress__thenShouldReturnIllegalArgumentException() {
        assertThrows(NullPointerException.class, () -> service.getCoordinates("1234AB" , null));
    }

}

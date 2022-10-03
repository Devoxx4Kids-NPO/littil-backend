package org.littil.api.coordinates.service;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.littil.mock.coordinates.service.WireMockSearchService;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
@QuarkusTestResource(WireMockSearchService.class)
class CoordinatesServiceTest {

    @Inject
    CoordinatesService service;

    @Test
    void givenGetCoordinates_thenShouldReturnCoordinates() {
        Optional<Coordinates> coordinates = service.getCoordinates("1234AB", "Address");
        assertThat(coordinates).isPresent();
    }

    @Test
    void givenGetCoordinatesNotFound_thenShouldNotBreakPersistenceFlow() {
        Optional<Coordinates> coordinates = service.getCoordinates("1234AB", WireMockSearchService.NOT_FOUND);
        assertThat(coordinates).isNotNull();
    }

    @Test
    void givenGetCoordinatesWithNullPostalCode_thenShouldThrowConstraintViolationException() {
        assertThrows(ConstraintViolationException.class, () -> service.getCoordinates(null, "address"));
    }

    @Test
    void givenGetCoordinatesWithBlankPostalCode_thenShouldThrowConstraintViolationException() {
        assertThrows(ConstraintViolationException.class, () -> service.getCoordinates("", "address"));
    }

    @Test
    void givenGetCoordinatesWithBlankAddress_thenShouldThrowConstraintViolationException() {
        assertThrows(ConstraintViolationException.class, () -> service.getCoordinates("1234AB", ""));
    }

    @Test
    void givenGetCoordinatesWithNullAddress_thenShouldThrowConstraintViolationException() {
        assertThrows(ConstraintViolationException.class, () -> service.getCoordinates("1234AB", null));
    }
}

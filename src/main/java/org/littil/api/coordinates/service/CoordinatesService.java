package org.littil.api.coordinates.service;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotEmpty;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
@Slf4j
public class CoordinatesService {

    @Inject
    @RestClient
    SearchService coordinatesService;

    public Optional<Coordinates> getCoordinates(@NotEmpty final String postalCode) {
        final Set<Coordinates> coordinatesSet = coordinatesService.getCoordinatesByAddress(postalCode, "json");
        
        if (coordinatesSet.isEmpty()) {
            log.warn(String.format("Coordinates could not be fetched with postal code: %s." +
                    "This implies this address will not be found when searching within a radius.", postalCode));
        }

        return coordinatesSet.stream().findFirst();
    }
}
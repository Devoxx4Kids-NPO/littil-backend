package org.littil.api.coordinates.service;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotEmpty;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
@Slf4j
public class CoordinatesService {

    @Inject
    @RestClient
    SearchService coordinatesService;

    public Optional<Coordinates> getCoordinates(@NotEmpty final String postalCode, @NotEmpty final String address) {
        final Set<Coordinates> coordinatesSet = coordinatesService.getCoordinatesByAddress(postalCode, address, "json");

        if (coordinatesSet.isEmpty()) {
            log.warn(String.format("Coordinates could not be fetched with postal code: %s and address: %s." +
                    "This implies this address will not be found when searching within a radius.", postalCode, address));
        }

        return coordinatesSet.stream().findFirst();
    }
}
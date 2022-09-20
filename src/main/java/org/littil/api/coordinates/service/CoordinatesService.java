package org.littil.api.coordinates.service;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Set;

@ApplicationScoped
@Slf4j
public class CoordinatesService {

    public static final String FORMAT = "json";

    @Inject
    @RestClient
    private SearchService coordinatesService;

    public Coordinates getCoordinates(String postalCode, String address) {
        if(postalCode.isBlank() || address.isBlank()) {
            throwException(postalCode, address);
        }
        Set<Coordinates> coordinatesSet = coordinatesService.getCoordinatesByAddress(postalCode, address, FORMAT);
        if(coordinatesSet.isEmpty()) {
            throwException(postalCode, address);
        }
        return coordinatesSet.iterator().next();
    }

    private void throwException(String postalCode, String address) {
        log.warn("Coordinates could not be fetched with postal code: " + postalCode + " and address " + address);
        throw new IllegalArgumentException();
    }
}
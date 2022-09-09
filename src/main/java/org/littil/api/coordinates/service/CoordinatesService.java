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

    public Coordinates getCoordinates(String postalcode, String adres) {
        Set<Coordinates> coordinatesSet = coordinatesService.getCoordinatesByAdres(postalcode, adres, FORMAT);
        if(coordinatesSet.isEmpty()) {
            log.warn("Coordinates could not be fetched with postalcode: " + postalcode + " and adres " + adres);
            throw new IllegalArgumentException();
        }
        return coordinatesSet.iterator().next();
    }
}
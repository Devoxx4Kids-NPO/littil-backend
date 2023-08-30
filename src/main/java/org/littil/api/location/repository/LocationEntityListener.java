package org.littil.api.location.repository;

import org.littil.api.coordinates.service.Coordinates;
import org.littil.api.coordinates.service.CoordinatesService;
import org.littil.api.location.LocationConstants;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.util.Optional;

@ApplicationScoped
public class LocationEntityListener {

    @Inject
    CoordinatesService coordinatesService;

    @PrePersist
    @PreUpdate
    public void prePersistOrUpdate(final LocationEntity location) {
        final Optional<Coordinates> coordinates = coordinatesService.getCoordinates(location.getPostalCode());
        coordinates.ifPresent(c -> {
            location.setLatitude(convertToIntForAlgorithm(c.getLat()));
            location.setLongitude(convertToIntForAlgorithm(c.getLon()));
        });
    }

    private int convertToIntForAlgorithm(double coordinate) {
        return (int) (coordinate * LocationConstants.CONVERT_COORDINATE_RATIO);
    }
}
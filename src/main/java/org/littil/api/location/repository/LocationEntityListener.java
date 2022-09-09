package org.littil.api.location.repository;

import org.littil.api.coordinates.service.Coordinates;
import org.littil.api.coordinates.service.CoordinatesService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

@ApplicationScoped
public class LocationEntityListener {

    @Inject
    CoordinatesService coordinatesService;

    private static final int INT_FACTOR = 10000;

    @PrePersist
    @PreUpdate
    public void prePersistOrUpdate(LocationEntity location) {
        Coordinates coordinates = coordinatesService.getCoordinates(location.getPostalCode(), location.getAddress());
        location.setLatitude(castToInt(coordinates.getLat()));
        location.setLongitude(castToInt(coordinates.getLon()));
    }

    private int castToInt(Double coordinate) {
        return (int) (coordinate * INT_FACTOR);
    }
}
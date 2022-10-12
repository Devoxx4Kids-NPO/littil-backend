package org.littil.api.location.repository;

import org.littil.api.coordinates.service.Coordinates;
import org.littil.api.coordinates.service.CoordinatesService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
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
            location.setLatitude(c.getLat());
            location.setLongitude(c.getLon());
        });
    }
}
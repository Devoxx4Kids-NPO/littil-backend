package org.littil.api.location.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class LocationRepository implements PanacheRepositoryBase<LocationEntity, UUID> {
}

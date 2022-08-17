package org.littil.api.location.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import org.littil.api.school.repository.SchoolEntity;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class LocationRepository implements PanacheRepositoryBase<LocationEntity, UUID> {

}

package org.littil.api.school.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import org.littil.api.location.repository.LocationEntity;
import org.littil.api.location.repository.LocationRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class SchoolRepository implements PanacheRepositoryBase<SchoolEntity, UUID> {

    @Inject
    LocationRepository locationRepository;

    public List<SchoolEntity> findBySchoolNameLike(final String name) {
        String searchInput = "%" + name + "%";
        return list("name like ?1", searchInput );
    }

    public Optional<SchoolEntity> findByLocation(final LocationEntity location) {
        return find("location", location).firstResultOptional();
    }

    public Optional<SchoolEntity> findByLocationId(UUID locationId) {
        LocationEntity location = locationRepository.findById(locationId);
        return findByLocation(location);
    }
}

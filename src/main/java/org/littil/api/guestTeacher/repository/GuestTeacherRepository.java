package org.littil.api.guestTeacher.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import org.littil.api.location.repository.LocationEntity;
import org.littil.api.location.repository.LocationRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class GuestTeacherRepository implements PanacheRepositoryBase<GuestTeacherEntity, UUID> {

    @Inject
    LocationRepository locationRepository;

    public List<GuestTeacherEntity> findBySurnameLike(final String name) {
        String searchInput = "%" + name + "%";
        return list("surname like ?1", searchInput);
    }

    public Optional<GuestTeacherEntity> findByLocation(final LocationEntity location) {
        return find("location", location).firstResultOptional();
    }

    public Optional<GuestTeacherEntity> findByLocationId(final UUID locationId) {
        LocationEntity location = locationRepository.findById(locationId);
        return findByLocation(location);
    }
}

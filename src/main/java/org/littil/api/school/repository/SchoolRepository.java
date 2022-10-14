package org.littil.api.school.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import org.littil.api.location.repository.LocationEntity;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class SchoolRepository implements PanacheRepositoryBase<SchoolEntity, UUID> {

    public List<SchoolEntity> findBySchoolNameLike(final String name) {
        String searchInput = "%" + name + "%";
        return list("school_name like ?1", searchInput );
    }

    public Optional<SchoolEntity> findByLocation(final LocationEntity location) {
        return find("location", location).firstResultOptional();
    }
}

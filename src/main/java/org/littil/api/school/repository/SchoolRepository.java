package org.littil.api.school.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class SchoolRepository implements PanacheRepositoryBase<SchoolEntity, UUID> {

    public List<SchoolEntity> findBySchoolNameLike(final String name) {
        String searchInput = "%" + name + "%";
        return list("school_name like ?1", searchInput );
    }
}

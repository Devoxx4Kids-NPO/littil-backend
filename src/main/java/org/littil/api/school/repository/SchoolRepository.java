package org.littil.api.school.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import javax.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class SchoolRepository implements PanacheRepositoryBase<SchoolEntity, UUID> {

    public Optional<SchoolEntity> findByName(final String name) {
        return find("school_name", name).firstResultOptional();
    }
}

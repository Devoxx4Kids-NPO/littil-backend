package org.littil.api.teacher.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class TeacherRepository implements PanacheRepositoryBase<TeacherEntity, UUID> {

    public List<TeacherEntity> findByName(final String name) {
        return find("surname", name).list();
    }
}

package org.littil.api.guestTeacher.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class GuestTeacherRepository implements PanacheRepositoryBase<GuestTeacherEntity, UUID> {

    public List<GuestTeacherEntity> findByName(final String name) {
        return find("surname", name).list();
    }
}

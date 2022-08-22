package org.littil.api.guestTeacher.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class GuestTeacherRepository implements PanacheRepositoryBase<GuestTeacherEntity, UUID> {

    public List<GuestTeacherEntity> findBySurnameLike(final String name) {
        String searchInput = "%" + name + "%";
        return list("surname like ?1", searchInput);
    }
}

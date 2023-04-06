package org.littil.api.guestTeacher.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import javax.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class GuestTeacherModuleRepository implements PanacheRepositoryBase<GuestTeacherModuleEntity, UUID> {

}

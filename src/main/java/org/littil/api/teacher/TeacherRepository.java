package org.littil.api.teacher;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import java.util.UUID;

@ApplicationScoped
public class TeacherRepository implements PanacheRepositoryBase<Teacher, UUID> {

    public Teacher findByName(final String name){
        return find("name", name).firstResult();
    }
}

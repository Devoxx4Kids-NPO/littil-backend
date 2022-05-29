package org.littil.api.school;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import java.util.UUID;

@ApplicationScoped
public class SchoolRepository implements PanacheRepositoryBase<School, UUID> {

    public School findByName(final String name){
        return find("name", name).firstResult();
    }
}

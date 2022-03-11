package org.littil.api.school;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class SchoolRepository implements PanacheRepository<School> {

    // remove comments below, just a remark
    // out of the box you get repository methods like listAll() see: https://thorben-janssen.com/introduction-panache/
    // beside that you can write custom methods like findByName below
    public School findByName(final String name){
        return find("name", name).firstResult();
    }
}

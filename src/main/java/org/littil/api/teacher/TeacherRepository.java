package org.littil.api.teacher;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class TeacherRepository implements PanacheRepository<Teacher> {

    // remove comments below, just a remark
    // out of the box you get repository methods like listAll() see: https://thorben-janssen.com/introduction-panache/
    // beside that you can write custom methods like findByName below
    public Teacher findByName(final String name){
        return find("name", name).firstResult();
    }
}

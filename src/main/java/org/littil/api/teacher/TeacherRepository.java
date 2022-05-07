package org.littil.api.teacher;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class TeacherRepository implements PanacheRepository<Teacher> {

    // remove comments below, just a remark
    // out of the box you get repository methods like listAll() see: https://thorben-janssen.com/introduction-panache/
    // beside that you can write custom methods like findBySurname below
    public PanacheQuery<Teacher> findBySurname(final String surname){
        return find("surname", surname);
    }
    
	public Optional<Teacher> findByEmail(String email) {
		return find("email", email).firstResultOptional();
	}
}

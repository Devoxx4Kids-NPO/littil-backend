package org.littil.api.school;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Set;

@Path("/school")
public class SchoolResource {

    private Set<School> schools = Collections.newSetFromMap(Collections.synchronizedMap(new LinkedHashMap<>()));

    public SchoolResource() {
        schools.add(new School("OBS", "1234AB", "Freddie",
                "freddie@gmail.com"));
        schools.add(new School("Geen-OBS", "4321-BA", "Brian",
                "brian@gmail.com"));
    }

    @GET
    public Set<School> list() {
        return schools;
    }

    @POST
    public Set<School> add(School school) {
        schools.add(school);
        return schools;
    }

    @DELETE
    public Set<School> delete(School school) {
        schools.removeIf(existingSchool -> existingSchool.getName().contentEquals(school.getName()));
        return schools;
    }
}

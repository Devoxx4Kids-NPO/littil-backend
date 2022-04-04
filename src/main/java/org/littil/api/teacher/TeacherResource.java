package org.littil.api.teacher;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Set;

@Path("/api/v1/teacher")
public class TeacherResource {

    private Set<Teacher> teachers = Collections.newSetFromMap(Collections.synchronizedMap(new LinkedHashMap<>()));

    TeacherResource () {
        teachers.add(new Teacher(null,"Phil", "Lead", "phil@gmail.com", "2345AB","nl",null));
        teachers.add(new Teacher(null,"Darrell", "Guitar", "darrell@gmail.com", "3456AB","nl",null));
    }

    @GET
    public Set<Teacher> list() {
        return teachers;
    }

    @POST
    public Set<Teacher> add(Teacher teacher) {
        teachers.add(teacher);
        return teachers;
    }

    @DELETE
    public Set<Teacher> delete(Teacher teacher) {
        teachers.removeIf(existingTeacher -> existingTeacher.getEmail().contentEquals(teacher.getEmail()));
        return teachers;
    }
}

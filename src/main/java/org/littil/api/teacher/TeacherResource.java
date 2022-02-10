package org.littil.api.teacher;

import org.littil.api.school.School;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Set;

@Path("/teacher")
public class TeacherResource {

    private Set<Teacher> teachers = Collections.newSetFromMap(Collections.synchronizedMap(new LinkedHashMap<>()));

    TeacherResource () {
        teachers.add(new Teacher("Phil", "Lead", "phil@gmail.com", "2345AB"));
        teachers.add(new Teacher("Darrell", "Guitar", "darrell@gmail.com", "3456AB"));
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

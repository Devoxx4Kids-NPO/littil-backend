package org.littil.api.teacher;

import lombok.RequiredArgsConstructor;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.Set;

@Path("/api/v1/teacher")
@RequiredArgsConstructor
public class TeacherResource {

    private final TeacherService teacherService;

    @GET
    public Set<TeacherDto> list() {
        return teacherService.getAll();
    }

    @GET
    @Path("{id}")
    public TeacherDto get(@PathParam("id") final Long id) {
        return teacherService.getTeacherById(id);
    }
    
    @GET
    @Path("name/{name}")
    public TeacherDto get(@PathParam("name")final String name) {
        return teacherService.getTeacherByName(name);
    }
    
    @POST
    public Set<TeacherDto> add(final TeacherDto teacherDto) {
        return teacherService.saveTeacher(teacherDto);
    }

    @DELETE
    public Set<TeacherDto> delete(final TeacherDto teacherDto) {
        return teacherService.deleteTeacher(teacherDto);
    }
}

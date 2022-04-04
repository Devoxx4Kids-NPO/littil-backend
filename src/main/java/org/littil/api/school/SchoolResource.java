package org.littil.api.school;

import lombok.RequiredArgsConstructor;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.Set;

@Path("/api/v1/school")
@RequiredArgsConstructor
public class SchoolResource {

    private final SchoolService schoolService;

    @GET
    public Set<SchoolDto> list() {
        return schoolService.getAll();
    }

    @GET
    @Path("{id}")
    public SchoolDto get(@PathParam("id") final Long id) {
        return schoolService.getSchoolById(id);
    }
    
    @GET
    @Path("name/{name}")
    public SchoolDto get(@PathParam("name")final String name) {
        return schoolService.getSchoolByName(name);
    }
    
    @POST
    public Set<SchoolDto> add(final SchoolDto schoolDto) {
        return schoolService.saveSchool(schoolDto);
    }

    @DELETE
    public Set<SchoolDto> delete(final SchoolDto schoolDto) {
        return schoolService.deleteSchool(schoolDto);
    }
}

package org.littil.api.school;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.Set;

@Path("/school")
public class SchoolResource {

    @Inject
    private SchoolService schoolService;
    
    @GET
    public Set<SchoolDto> list() {
        return schoolService.getAll();
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

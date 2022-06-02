package org.littil.api.school.api;

import io.smallrye.common.constraint.NotNull;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.littil.api.school.School;
import org.littil.api.school.SchoolService;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Path("/api/v1/school")
@RequiredArgsConstructor
public class SchoolController {

    private final SchoolService schoolService;
    private final SchoolMapper mapper;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Fetch all available schools")
    @APIResponse(responseCode = "200", description = "Ok")
    public Response list() {
        List<SchoolResource> schools = schoolService.findAll().stream()
                .map(mapper::schoolToSchoolResource)
                .toList();

        return Response.ok(schools).build();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Fetch a specific school by Id")
    @APIResponse(responseCode = "200", description = "School with Id found.")
    @APIResponse(responseCode = "404", description = "School with specific Id was not found.")
    public Response get(@PathParam("id") final UUID id) {
        Optional<School> school = schoolService.getSchoolById(id);
        Optional<SchoolResource> resource = school.map(mapper::schoolToSchoolResource);

        return resource.map(r -> Response.ok(resource).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("name/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Fetch a specific school via name")
    @APIResponse(responseCode = "200", description = "School with name found.")
    @APIResponse(responseCode = "404", description = "School with specific name was not found.")
    public Response getByName(@NotNull @PathParam("name") final String name) {
        Optional<School> school = schoolService.getSchoolByName(name);
        Optional<SchoolResource> resource = school.map(mapper::schoolToSchoolResource);

        return resource.map(r -> Response.ok(resource).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new school")
    @APIResponse(responseCode = "201", description = "School successfully created")
    @APIResponse(responseCode = "404", description = "Unable to fetch created school. Something went wrong.")
    @APIResponse(responseCode = "500", description = "Persistence error occurred. Failed to persist school.")
    public Response add(final SchoolUpsertResource schoolResource) {
        School persistedSchool = schoolService.saveSchool(schoolResource);
        SchoolResource resource = mapper.schoolToSchoolResource(persistedSchool);

        return Response.status(Response.Status.CREATED).entity(resource).build();
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "Delete a school specified with an Id")
    @APIResponse(responseCode = "200", description = "Successfully deleted the school.")
    @APIResponse(responseCode = "404", description = "The school to delete was not found.")
    public Response delete(@PathParam("id") UUID id) {
        schoolService.deleteSchool(id);
        return Response.ok().build();
    }
}

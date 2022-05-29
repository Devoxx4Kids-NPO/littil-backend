package org.littil.api.teacher.api;

import io.smallrye.common.constraint.NotNull;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.littil.api.teacher.Teacher;
import org.littil.api.teacher.TeacherService;
import org.littil.api.util.JsonHelper;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/v1/teacher")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    private final TeacherMapper mapper;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Fetch all available teachers")
    @APIResponse(responseCode = "200", description = "Ok")
    public Response list() {
        List<TeacherResource> teachers = teacherService.findAll().stream()
                .map(mapper::teacherToTeacherResource)
                .collect(Collectors.toList());

        return Response.ok(teachers).build();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Fetch a specific teacher by Id")
    @APIResponse(responseCode = "200", description = "Teacher with Id found.")
    @APIResponse(responseCode = "404", description = "Teacher with specific Id was not found.")
    public Response get(@PathParam("id") final UUID id) {
        Optional<Teacher> teacher = teacherService.getTeacherById(id);
        Optional<TeacherResource> resource = teacher.map(mapper::teacherToTeacherResource);

        return resource.map(r -> Response.ok(resource).build())
                        .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).entity(JsonHelper.toJson("Teacher not found.")).build());
    }
    
    @GET
    @Path("name/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Fetch a specific teacher via name")
    @APIResponse(responseCode = "200", description = "Teacher with name found.")
    @APIResponse(responseCode = "404", description = "Teacher with specific name was not found.")
    public Response getByName(@NotNull @PathParam("name") final String name) {
        Optional<Teacher> teacher = teacherService.getTeacherByName(name);
        Optional<TeacherResource> resource = teacher.map(mapper::teacherToTeacherResource);

        return resource.map(r -> Response.ok(resource).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).entity(JsonHelper.toJson("Teacher not found.")).build());
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new teacher")
    @APIResponse(responseCode = "201", description = "Teacher successfully created")
    @APIResponse(responseCode = "404", description = "Unable to fetch created teacher. Something went wrong.")
    @APIResponse(responseCode = "500", description = "Persistence error occurred. Failed to persist teacher.")
    public Response create(final TeacherUpsertResource teacherResource) {
        Teacher persistedTeacher = teacherService.saveTeacher(teacherResource);
        TeacherResource resource = mapper.teacherToTeacherResource(persistedTeacher);

        return Response.status(Response.Status.CREATED).entity(resource).build();
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "Delete a teacher specified with an Id")
    @APIResponse(responseCode = "200", description = "Successfully deleted the teacher.")
    @APIResponse(responseCode = "404", description = "The teacher to delete was not found.")
    public Response delete(@PathParam("id") UUID id) {
        teacherService.deleteTeacher(id);
        return Response.ok().build();
    }
}

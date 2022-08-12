package org.littil.api.guestTeacher.api;

import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.littil.api.auth.authz.UserOwned;
import org.littil.api.exception.ErrorResponse;
import org.littil.api.exception.ServiceException;
import org.littil.api.guestTeacher.service.GuestTeacher;
import org.littil.api.guestTeacher.service.GuestTeacherService;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Path("/api/v1/guest-teachers")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@UserOwned
@Tag(name = "Teacher", description = "CRUD Operations")
public class GuestTeacherResource {

    @Inject
    GuestTeacherService guestTeacherService;

    @GET
    @Operation(summary = "Get all guest teachers")
    @APIResponse(
            responseCode = "200",
            description = "Get all guest teachers",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.ARRAY, implementation = GuestTeacher.class)
            )
    )
    public Response list() {
        List<GuestTeacher> guestTeachers = guestTeacherService.findAll();

        return Response.ok(guestTeachers).build();
    }

    @GET
    @Path("{id}")
    @Operation(summary = "Fetch a specific teacher by Id")
    @APIResponse(
            responseCode = "200",
            description = "Teacher with Id found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.OBJECT, implementation = GuestTeacher.class)
            )
    )
    @APIResponse(
            responseCode = "404",
            description = "Teacher with specific Id was not found.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
    )
    public Response get(@Parameter(name = "id", required = true) @PathParam("id") final UUID id) {
        Optional<GuestTeacher> teacher = guestTeacherService.getTeacherById(id);

        return teacher.map(r -> Response.ok(r).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/name/{name}")
    @Operation(summary = "Fetch teachers via name")
    @APIResponse(
            responseCode = "200",
            description = "Teachers with name found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.ARRAY, implementation = GuestTeacher.class)
            )
    )
    public Response getByName(@Parameter(name = "name", required = true) @PathParam("name") final String name) {
        List<GuestTeacher> guestTeachers = guestTeacherService.getTeacherByName(name);

        return Response.ok(guestTeachers).build();
    }

    @POST
    @Operation(summary = "Create a new teacher")
    @APIResponse(
            responseCode = "201",
            description = "Teacher successfully created",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.OBJECT, implementation = GuestTeacher.class)
            )
    )
    @APIResponse(
            responseCode = "400",
            description = "Validation errors occurred.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.OBJECT, implementation = ErrorResponse.class)
            )
    )
    @APIResponse(
            responseCode = "500",
            description = "Persistence error occurred. Failed to persist teacher.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
    )
    public Response create(@NotNull @Valid GuestTeacher guestTeacher) {
        //todo check whether user already has school or guest teacher attached. Then return error.
        GuestTeacher persistedGuestTeacher = guestTeacherService.saveTeacher(guestTeacher);
        URI uri = UriBuilder.fromResource(GuestTeacherResource.class)
                .path("/" + persistedGuestTeacher.getId()).build();
        return Response.created(uri).entity(persistedGuestTeacher).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update the teacher")
    @APIResponse(
            responseCode = "200",
            description = "Teacher successfully updated",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.OBJECT, implementation = GuestTeacher.class)
            )
    )
    @APIResponse(
            responseCode = "400",
            description = "Invalid teacher",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.OBJECT, implementation = ErrorResponse.class)
            )
    )
    @APIResponse(
            responseCode = "500",
            description = "Path variable Id does not match Teacher.id",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
    )
    @APIResponse(
            responseCode = "404",
            description = "No Teacher found for id provided",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
    )
    public Response put(@Parameter(name = "id", required = true) @PathParam("id") final UUID id, @NotNull @Valid GuestTeacher guestTeacher) {
        if (!Objects.equals(id, guestTeacher.getId())) {
            throw new ServiceException("Path variable id does not match Teacher.id");
        }

        GuestTeacher updatedGuestTeacher = guestTeacherService.update(guestTeacher);
        return Response.ok(updatedGuestTeacher).build();
    }

    @DELETE
    @Path("{id}")
    @Operation(summary = "Delete a teacher specified with an Id")
    @APIResponse(
            responseCode = "200",
            description = "Successfully deleted the teacher.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
    )
    @APIResponse(
            responseCode = "404",
            description = "The teacher to delete was not found.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
    )
    public Response delete(@PathParam("id") UUID id) {
        guestTeacherService.deleteTeacher(id);
        return Response.ok().build();
    }
}

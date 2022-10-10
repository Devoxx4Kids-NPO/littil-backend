package org.littil.api.guestTeacher.api;

import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.littil.api.auth.TokenHelper;
import org.littil.api.auth.authz.GuestTeacherSecured;
import org.littil.api.exception.ErrorResponse;
import org.littil.api.guestTeacher.service.GuestTeacher;
import org.littil.api.guestTeacher.service.GuestTeacherMapper;
import org.littil.api.guestTeacher.service.GuestTeacherPublic;
import org.littil.api.guestTeacher.service.GuestTeacherService;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Path("/api/v1/guest-teachers")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@GuestTeacherSecured
@Tag(name = "Teacher", description = "CRUD Operations")
public class GuestTeacherResource {

    @Inject
    GuestTeacherService guestTeacherService;
    @Inject
    GuestTeacherMapper mapper;
    @Inject
    TokenHelper tokenHelper;

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
        List<GuestTeacherPublic> guestTeachers = guestTeacherService.findAll();

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
            description = "Teacher with specific Id was not found."
    )
    public Response get(@Parameter(name = "id", required = true) @PathParam("id") final UUID id) {
        Optional<GuestTeacherPublic> teacher = guestTeacherService.getTeacherById(id);

        return teacher.map(r -> Response.ok(r).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/owned/{id}")
    @Operation(summary = "Fetch a specific teacher by Id which is owned by the user")
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
            description = "Teacher with specific Id was not found."
    )
    public Response getTeacherOwnedByUser(@Parameter(name = "id", required = true) @PathParam("id") final UUID id) {
        Optional<GuestTeacher> teacher = guestTeacherService.getUserOwnedTeacherById(id, tokenHelper.getCurrentUserId());

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
    @APIResponse(
            responseCode = "404",
            description = "Teacher with specific name was not found."
    )
    public Response getByName(@Parameter(name = "name", required = true) @PathParam("name") final String name) {
        List<GuestTeacherPublic> guestTeachers = guestTeacherService.getTeacherByName(name);
        return Response.ok(guestTeachers).build();
    }

    @PUT
    @Operation(summary = "Create or update a teacher")
    @APIResponse(
            responseCode = "200",
            description = "Teacher successfully created or updated",
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
            responseCode = "404",
            description = "No Teacher found for id provided"
    )
    @APIResponse(
            responseCode = "500",
            description = "Persistence error occurred. Failed to persist teacher.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
    )
    public Response createOrUpdate(@NotNull @Valid GuestTeacherPostResource guestTeacher) {
        if (tokenHelper.hasUserAuthorizations()) {
            return Response.status(Response.Status.CONFLICT)
                    .build();
        }

        GuestTeacher persistedGuestTeacher = guestTeacherService.saveOrUpdate(mapper.toDomain(guestTeacher), tokenHelper.getCurrentUserId());
        URI uri = UriBuilder.fromResource(GuestTeacherResource.class)
                .path("/" + persistedGuestTeacher.getId()).build();
        return Response.ok(uri).entity(persistedGuestTeacher).build();
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
    @APIResponse(
            responseCode = "401",
            description = "Current user is not owner of this guest teacher profile"
    )
    public Response delete(@PathParam("id") UUID id) {
        guestTeacherService.deleteTeacher(id, tokenHelper.getCurrentUserId());
        return Response.ok().build();
    }
}

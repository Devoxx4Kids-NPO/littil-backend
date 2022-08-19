package org.littil.api.school.api;

import io.quarkus.security.Authenticated;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.littil.api.auth.TokenHelper;
import org.littil.api.auth.authz.SchoolSecured;
import org.littil.api.exception.ErrorResponse;
import org.littil.api.school.service.School;
import org.littil.api.school.service.SchoolMapper;
import org.littil.api.school.service.SchoolService;

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
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Path("/api/v1/schools")
@RequestScoped
@Slf4j
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@SchoolSecured
@Tag(name = "School", description = "CRUD Operations")
public class SchoolResource {

    @Inject
    SchoolService schoolService;
    @Inject
    SchoolMapper mapper;
    @Inject
    TokenHelper tokenHelper;

    @GET
    @Operation(summary = "Get all schools")
    @APIResponse(
            responseCode = "200",
            description = "Get all schools",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.ARRAY, implementation = School.class)
            )
    )
    public Response list() {
        List<School> schools = schoolService.findAll();
        return Response.ok(schools).build();
    }

    @GET
    @Path("{id}")
    @Operation(summary = "Fetch a specific school by Id")
    @APIResponse(
            responseCode = "200",
            description = "School with Id found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.OBJECT, implementation = School.class)
            )
    )
    @APIResponse(
            responseCode = "404",
            description = "School with specific Id was not found."
    )
    public Response get(@Parameter(name = "id", required = true) @PathParam("id") final UUID id) {
        Optional<School> school = schoolService.getSchoolById(id);
        return school.map(r -> Response.ok(r).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/name/{name}")
    @Operation(summary = "Fetch schools via name")
    @APIResponse(
            responseCode = "200",
            description = "Schools with name found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.ARRAY, implementation = School.class)
            )
    )
    @APIResponse(
            responseCode = "404",
            description = "School with specific name was not found."
    )
    public Response getByName(@Parameter(name = "name", required = true) @PathParam("name") final String name) {
        List<School> schools = schoolService.getSchoolByName(name);

        return Response.ok(schools).build();
    }

    @POST
    @Operation(summary = "Create a new school")
    @APIResponse(
            responseCode = "201",
            description = "School successfully created",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.OBJECT, implementation = School.class)
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
            responseCode = "409",
            description = "Current user already either a school or guest teacher profile attached",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.OBJECT, implementation = ErrorResponse.class)
            )
    )
    @APIResponse(
            responseCode = "500",
            description = "Persistence error occurred. Failed to persist school."
    )
    public Response create(@NotNull @Valid SchoolPostResource school) {
        if (tokenHelper.hasUserAuthorizations()) {
            return Response.status(Response.Status.CONFLICT)
                    .build();
        }

        School persistedSchool = schoolService.saveSchool(mapper.toDomain(school), tokenHelper.getCurrentUserId());
        URI uri = UriBuilder.fromResource(SchoolResource.class)
                .path("/" + persistedSchool.getId()).build();
        return Response.created(uri).entity(persistedSchool).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update the school")
    @APIResponse(
            responseCode = "200",
            description = "School successfully updated",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.OBJECT, implementation = School.class)
            )
    )
    @APIResponse(
            responseCode = "400",
            description = "Invalid school",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.OBJECT, implementation = ErrorResponse.class)
            )
    )
    @APIResponse(
            responseCode = "500",
            description = "Path variable Id does not match School.id",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.OBJECT, implementation = ErrorResponse.class)
            )
    )
    @APIResponse(
            responseCode = "404",
            description = "No School found for  provided id"
    )
    @APIResponse(
            responseCode = "401",
            description = "Current user is not owner of this school"
    )
    public Response put(@Parameter(name = "id", required = true) @PathParam("id") final UUID id, @NotNull @Valid School school) {
        if (!Objects.equals(id, school.getId())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Path variable id does not match School.id")
                    .build();
        }

        School updatedSchool = schoolService.update(school);
        return Response.ok(updatedSchool).build();
    }

    @DELETE
    @Path("{id}")
    @Operation(summary = "Delete a school specified with an Id")
    @APIResponse(
            responseCode = "200",
            description = "Successfully deleted the school.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
    )
    @APIResponse(
            responseCode = "404",
            description = "The school to delete was not found."
    )
    @APIResponse(
            responseCode = "401",
            description = "Current user is not owner of this school"
    )
    public Response delete(@PathParam("id") UUID id) {
        schoolService.deleteSchool(id, tokenHelper.getCurrentUserId());
        return Response.ok().build();
    }
}

package org.littil.api.userSetting.api;

import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.littil.api.auth.TokenHelper;
import org.littil.api.exception.ErrorResponse;
import org.littil.api.exception.ServiceException;
import org.littil.api.school.service.School;
import org.littil.api.userSetting.service.UserSetting;
import org.littil.api.userSetting.service.UserSettingService;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Path("/api/v1/user-settings")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@Tag(name = "User settings", description = "CRUD Operations")
public class UserSettingResource {

    @Inject
    UserSettingService service;

    @Inject
    TokenHelper tokenHelper;

    @GET
    @Operation(summary = "Get all user settings for current user")
    @APIResponse(
            responseCode = "200",
            description = "Get all user settings",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.ARRAY, implementation = UserSetting.class)
            )
    )
    public Response list() {
        final List<UserSetting> userSettings = service.findAll(tokenHelper.getCurrentUserId());
        return Response.ok(userSettings).build();
    }

    @GET
    @Path("{key}")
    @Operation(summary = "Get user setting by key for the current user")
    @APIResponse(
            responseCode = "200",
            description = "Get all available settings of the current user",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.ARRAY, implementation = School.class)
            )
    )
    public Response get(@Parameter(name = "key", required = true) @PathParam("key") final String id) {
        final Optional<UserSetting> userSetting = service.getUserSettingByKey(id, tokenHelper.getCurrentUserId());

        return userSetting.map(r -> Response.ok(r).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    @Operation(summary = "Create a new user setting")
    @APIResponse(
            responseCode = "201",
            description = "User setting successfully created",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.OBJECT, implementation = UserSetting.class)
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
            description = "Persistence error occurred. Failed to persist user setting.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
    )
    public Response create(@NotNull @Valid final UserSetting userSetting) {
        final UserSetting savedUserSetting = service.save(userSetting, tokenHelper.getCurrentUserId());
        final URI uri = UriBuilder.fromResource(UserSettingResource.class)
                .path("/" + savedUserSetting.getKey()).build();
        return Response.created(uri).entity(savedUserSetting).build();
    }

    @PUT
    @Path("{key}")
    @Operation(summary = "Update user setting by key for the current user")
    @APIResponse(
            responseCode = "200",
            description = "User setting successfully updated",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.OBJECT, implementation = UserSetting.class)
            )
    )
    @APIResponse(
            responseCode = "400",
            description = "Invalid user setting",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.OBJECT, implementation = ErrorResponse.class)
            )
    )
    @APIResponse(
            responseCode = "500",
            description = "Path variable Key does not match UserSetting.key",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
    )
    @APIResponse(
            responseCode = "404",
            description = "No user setting found for key provided and current user",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
    )
    public Response update(@Parameter(name = "key", required = true) @PathParam("key") final String key, @NotNull @Valid final UserSetting userSetting) {
        if (!Objects.equals(key, userSetting.getKey())) {
            throw new ServiceException("Path variable key does not match UserSetting.key");
        }
        final UUID userId = tokenHelper.getCurrentUserId();
        final Optional<UserSetting> existingUserSetting = service.getUserSettingByKey(key, userId);
        return existingUserSetting.map(s -> service.update(userSetting, userId))
                .map(updatedUserSetting -> Response.ok(updatedUserSetting).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @DELETE
    @Path("{key}")
    @Operation(summary = "Delete a user setting specified with an key")
    @APIResponse(
            responseCode = "200",
            description = "Successfully deleted the user setting.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
    )
    @APIResponse(
            responseCode = "404",
            description = "The user setting to delete was not found.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
    )
    public Response delete(@PathParam("key") final String key) {
        service.delete(key, tokenHelper.getCurrentUserId());
        return Response.ok().build();
    }
}

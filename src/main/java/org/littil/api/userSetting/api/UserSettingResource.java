package org.littil.api.userSetting.api;

import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.littil.api.auth.authz.UserOwned;
import org.littil.api.auth.authz.UserSettingInterceptor;
import org.littil.api.school.service.School;
import org.littil.api.userSetting.service.UserSetting;
import org.littil.api.userSetting.service.UserSettingService;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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

    @GET
    @Path("{key}")
    @UserOwned(filter = UserSettingInterceptor.class)
    @Operation(summary = "Get user setting by key for the current user")
    @APIResponse(
            responseCode = "200",
            description = "Get all available settings of the current user",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.ARRAY, implementation = School.class)
            )
    )
    public Response get(@Parameter(name = "key", required = true) @PathParam("key") final UUID id) {
        Optional<UserSetting> userSetting = service.getUserSettingByKey(id);

        return userSetting.map(r -> Response.ok(r).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

}

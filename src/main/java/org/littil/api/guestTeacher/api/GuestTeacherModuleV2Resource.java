package org.littil.api.guestTeacher.api;

import io.quarkus.security.Authenticated;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.littil.api.auth.authz.GuestTeacherSecured;
import org.littil.api.guestTeacher.service.GuestTeacherModuleService;
import org.littil.api.module.service.Module;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

@Path("/api/v2/guest-teachers")
@RequestScoped
@Slf4j
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@GuestTeacherSecured
@Tag(name = "Teacher Modules", description = "CRUD Operations")
public class GuestTeacherModuleV2Resource {

    @Inject
    GuestTeacherModuleService guestTeacherModuleService;

    @GET
    @Path("{id}/modules")
    @Operation(summary = "Fetch the list of modules for a specific guestTeacher")
    @APIResponse(
            responseCode = "200",
            description = "Modules for guestTeacher with Id found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.ARRAY, implementation = Module.class)
            )
    )
    @APIResponse(
            responseCode = "404",
            description = "GuestTeacher with specific Id was not found."
    )
    @APIResponse(
            responseCode = "401",
            description = "Current user is not owner of this guestTeacher"
    )
    public Response getGuestTeacherModules(@PathParam("id") final UUID id) {
        List<Module> guestTeacherModules = guestTeacherModuleService.getGuestTeacherModulesByGuestTeacherId(id);
        return  Response.ok(guestTeacherModules).build();
    }

    @POST
    @Path("{id}/modules")
    @Operation(summary = "Update the list of modules for a specific guestTeacher")
    @APIResponse(
            responseCode = "200",
            description = "Successfully updated the modules for the given guestTeacher.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
    )
    @APIResponse(
            responseCode = "404",
            description = "The guestTeacher or one of the modules was not found."
    )
    @APIResponse(
            responseCode = "401",
            description = "Current user is not owner of this guestTeacher")
    public Response saveGuestTeacherModules(@PathParam("id") final UUID id, @NotNull @Valid List<String> modules) {
        guestTeacherModuleService.save(id, modules);
        return Response.ok().build();
    }

}

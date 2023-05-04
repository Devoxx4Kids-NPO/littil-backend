package org.littil.api.guestTeacher.api;

import io.quarkus.security.Authenticated;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
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

@Path("/api/v1/guest-teachers")
@RequestScoped
@Slf4j
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@GuestTeacherSecured
@Tag(name = "Teacher Modules", description = "CRUD Operations")
public class GuestTeacherModuleResource {

    @Inject
    GuestTeacherModuleService guestTeacherModuleService;

    @GET
    @Path("{guestTeacher_id}/modules")
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
    public Response getGuestTeacherModules(@Parameter(name = "guestTeacher_id", required = true) @PathParam("guestTeacher_id") final UUID id) {
        List<Module> guestTeacherModules = guestTeacherModuleService.getGuestTeacherModulesByGuestTeacherId(id);
        return  Response.ok(guestTeacherModules).build();
    }

    @DELETE
    @Path("{guestTeacher_id}/modules/{module_id}")
    @Operation(summary = "Delete a module with an id for a given guestTeacher")
    @APIResponse(
            responseCode = "200",
            description = "Successfully deleted the module for the given guestTeacher.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
    )
    @APIResponse(
            responseCode = "404",
            description = "The guestTeacher or module to delete was not found."
    )
    @APIResponse(
            responseCode = "401",
            description = "Current user is not owner of this guestTeacher"
    )
    public Response deleteGuestTeacherModule(@Parameter(name = "guestTeacher_id", required = true) @PathParam("guestTeacher_id") UUID guestTeacher_id,
                                       @Parameter(name = "module_id", required = true) @PathParam("module_id") UUID module_id) {
        guestTeacherModuleService.deleteGuestTeacherModule(guestTeacher_id, module_id);
        return Response.ok().build();
    }

    @POST
    @Path("{guestTeacher_id}/modules")
    @Operation(summary = "Add an existing module to the list of modules for a specific guestTeacher")
    @APIResponse(
            responseCode = "200",
            description = "Successfully added the module for the given guestTeacher.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
    )
    @APIResponse(
            responseCode = "404",
            description = "The guestTeacher or module to add was not found."
    )
    @APIResponse(
            responseCode = "401",
            description = "Current user is not owner of this guestTeacher"
    )public Response saveGuestTeacherModule(@Parameter(name = "guestTeacher_id", required = true) @PathParam("guestTeacher_id") UUID guestTeacherId,  @NotNull @Valid Module module) {
        guestTeacherModuleService.save(guestTeacherId, module);
        return Response.status(Response.Status.CREATED).build();
    }

}

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

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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

    @DELETE
    @Path("{id}/modules/{module_id}")
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
    public Response deleteGuestTeacherModule(@PathParam("id") UUID id, @PathParam("module_id") UUID moduleId) {
        guestTeacherModuleService.deleteGuestTeacherModule(id, moduleId);
        return Response.ok().build();
    }

    @POST
    @Path("{id}/modules")
    @Operation(summary = "Add an existing module to the list of modules for a specific guestTeacher")
    @APIResponse(
            responseCode = "201",
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
    )public Response saveGuestTeacherModule(@PathParam("id") UUID id,  @NotNull @Valid Module module) {
        guestTeacherModuleService.save(id, module);
        return Response.status(Response.Status.CREATED).build();
    }

}

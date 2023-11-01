package org.littil.api.school.api;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.littil.api.auth.authz.SchoolSecured;
import org.littil.api.module.service.Module;
import org.littil.api.school.service.SchoolModuleService;

import java.util.List;
import java.util.UUID;

@Path("/api/v2/schools")
@RequestScoped
@Slf4j
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@SchoolSecured
@Tag(name = "School Modules", description = "CRUD Operations")
public class SchoolModuleV2Resource {

    @Inject
    SchoolModuleService schoolModuleService;

    @GET
    @Path("{id}/modules")
    @Operation(summary = "Fetch the list of modules for a specific school")
    @APIResponse(
            responseCode = "200",
            description = "Modules for school with Id found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.ARRAY, implementation = Module.class)
            )
    )
    @APIResponse(
            responseCode = "404",
            description = "School with specific Id was not found."
    )
    @APIResponse(
            responseCode = "401",
            description = "Current user is not owner of this school"
    )
    public Response getSchoolModules(@PathParam("id") final UUID id) {
        List<Module> schoolModules = schoolModuleService.getSchoolModulesBySchoolId(id);
        return  Response.ok(schoolModules).build();
    }

    @POST
    @Path("{id}/modules")
    @Operation(summary = "Update the list of modules for a specific school")
    @APIResponse(
            responseCode = "200",
            description = "Successfully updated the modules for the given school.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
    )
    @APIResponse(
            responseCode = "404",
            description = "The school or one of the modules was not found."
    )
    @APIResponse(
            responseCode = "401",
            description = "Current user is not owner of this school"
    )public Response saveSchoolModules(@PathParam("id") UUID id, @NotNull @Valid List<String> modules) {
        schoolModuleService.save(id, modules);
        return Response.ok().build();
    }

}

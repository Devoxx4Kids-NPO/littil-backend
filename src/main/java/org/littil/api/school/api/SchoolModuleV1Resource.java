package org.littil.api.school.api;

import io.quarkus.security.Authenticated;
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

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

@Path("/api/v1/schools")
@RequestScoped
@Slf4j
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@SchoolSecured
@Tag(name = "School Modules", description = "CRUD Operations")
public class SchoolModuleV1Resource {

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

    @DELETE
    @Path("{id}/modules/{module_id}")
    @Operation(summary = "Delete a module with an id for a given school")
    @APIResponse(
            responseCode = "200",
            description = "Successfully deleted the module for the given school.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
    )
    @APIResponse(
            responseCode = "404",
            description = "The school or module to delete was not found."
    )
    @APIResponse(
            responseCode = "401",
            description = "Current user is not owner of this school"
    )
    public Response deleteSchoolModule(@PathParam("id") UUID id,
                                       @PathParam("module_id") UUID moduleId) {
        schoolModuleService.deleteSchoolModule(id, moduleId);
        return Response.ok().build();
    }

    @POST
    @Path("{id}/modules")
    @Operation(summary = "Add an existing module to the list of modules for a specific school")
    @APIResponse(
            responseCode = "201",
            description = "Successfully added the module for the given school.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
    )
    @APIResponse(
            responseCode = "404",
            description = "The school or module to add was not found."
    )
    @APIResponse(
            responseCode = "401",
            description = "Current user is not owner of this school"
    )public Response saveSchoolModule(@PathParam("id") UUID id, @NotNull @Valid Module module) {
        schoolModuleService.save(id, module);
        return Response.status(Response.Status.CREATED).build();
    }

}
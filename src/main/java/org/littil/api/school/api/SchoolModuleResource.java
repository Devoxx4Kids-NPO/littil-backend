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
import org.littil.api.auth.authz.SchoolSecured;
import org.littil.api.module.service.Module;
import org.littil.api.school.service.SchoolModuleService;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
public class SchoolModuleResource {

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
    public Response getSchoolModules(@Parameter(name = "school_id", required = true) @PathParam("id") final UUID id) {
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
    public Response deleteSchoolModule(@Parameter(name = "school_id", required = true) @PathParam("id") UUID school_id,
                                       @Parameter(name = "module_id", required = true) @PathParam("module_id") UUID module_id) {
        schoolModuleService.deleteSchoolModule(school_id, module_id);
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
    )public Response saveSchoolModule(@Parameter(name = "school_id", required = true) @PathParam("id") UUID schoolId,  @NotNull @Valid Module module) {
        schoolModuleService.save(schoolId, module);
        return Response.status(Response.Status.CREATED).build();
    }

}

package org.littil.api.search.api;

import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.littil.api.auth.authz.GuestTeacherSecured;
import org.littil.api.exception.ErrorResponse;
import org.littil.api.module.service.Module;
import org.littil.api.module.service.ModuleService;
import org.littil.api.search.service.SearchResult;
import org.littil.api.search.service.SearchService;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@Path("/api/v1/search")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@GuestTeacherSecured
@Tag(name = "Search", description = "Read Operations")
public class SearchResource {

    static final String MODULES_NOT_VALID = "List of modules contain invalid module(s)";

    @Inject
    SearchService searchService;

    @Inject
    ModuleService moduleService;

    @GET
    @Path("/")
    @Operation(summary = "Fetch a list of teachers or school ordered by distance")
    @APIResponse(
            responseCode = "200",
            description = "Fetch a list of teachers or school ordered by distance",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.ARRAY, implementation = SearchResult.class)
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
    public Response get(@QueryParam("lat")  double latitude,
                        @QueryParam("long") double longitude,
                        @QueryParam("userType") String userTypeInput,
                        @QueryParam("maxDistance") int maxDistance,
                        @QueryParam("expectedModules") List<String> expectedModules) {

        if (!validModules(expectedModules)) {
            return Response.status(Response.Status.BAD_REQUEST) //
                 .entity(new ErrorResponse(null,List.of(new ErrorResponse.ErrorMessage(MODULES_NOT_VALID)))).build();

        }
        Optional<UserType> expectedUserType = UserType.findByLabel(userTypeInput);
        List<SearchResult> searchResults = searchService.getSearchResults(latitude, longitude, expectedUserType, maxDistance, expectedModules);
        return Response.ok(searchResults).build();
    }

    private boolean validModules(List<String> expectedModules) {
        List<String> activeModules = moduleService.findAll().stream().map(Module::getName).toList();
        return expectedModules.stream() //
                .filter(name -> !activeModules.contains(name)) //
                .toList() //
                .isEmpty();
    }

}
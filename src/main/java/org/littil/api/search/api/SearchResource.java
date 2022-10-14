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
import org.littil.api.search.service.SearchResult;
import org.littil.api.search.service.SearchService;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@Path("/api/v1/search")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@GuestTeacherSecured
@Tag(name = "Search", description = "CRUD Operations")
public class SearchResource {

    @Inject
    SearchService searchService;

    @GET
    @Path("/")
    @Operation(summary = "Fetch a list of teachers or school ordered by distance")
    @APIResponse(
            responseCode = "200",
            description = "Fetch a list of teachers or school ordered by distance",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.OBJECT, implementation = SearchResult.class)
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
    public Response get(@QueryParam("lat") double latitude,
                        @QueryParam("long") double longitude,
                        @QueryParam("userType") String userTypeInput) {
        Optional<UserType> expectedUserType = UserType.findByLabel(userTypeInput);
        if(expectedUserType.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        List<SearchResult> searchResults = searchService.getSearchResultsBySearch(latitude, longitude,
                expectedUserType.get());

        return Response.ok(searchResults).build();
    }

}
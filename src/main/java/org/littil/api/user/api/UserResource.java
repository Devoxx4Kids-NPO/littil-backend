package org.littil.api.user.api;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.littil.api.exception.ErrorResponse;
import org.littil.api.mail.MailService;
import org.littil.api.user.service.User;
import org.littil.api.user.service.UserMapper;
import org.littil.api.user.service.UserService;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Path("/api/v1/users")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Users", description = "CRUD Operations for users")
public class UserResource {
    @Inject
    UserService userService;
    @Inject
    UserMapper userMapper;
    @Inject
    MailService mailService;

    // todo remove (for now easy for testing)
    @GET
    @Path("user")
    @Operation(summary = "Get all users")
    @APIResponse(responseCode = "200", description = "Get all users", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.ARRAY, implementation = User.class)))
    public Response list() {
        List<User> users = userService.listUsers();
        return Response.ok(users)
                .build();
    }

    @GET
    @Path("user/{id}")
    @Operation(summary = "Fetch a specific user by Id")
    @APIResponse(responseCode = "200", description = "User with Id found.", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.OBJECT, implementation = User.class)))
    @APIResponse(responseCode = "404", description = "User with specific Id was not found.", content = @Content(mediaType = MediaType.APPLICATION_JSON))
    public Response get(@Parameter(name = "id", required = true) @PathParam("id") final UUID id) {
        Optional<User> user = userService.getUserById(id);
        if (user.isPresent()) {
            return Response.ok(user)
                    .build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @POST
    @Path("user")
    @Operation(summary = "Create a new user")
    @APIResponse(
            responseCode = "201",
            description = "User successfully created",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.OBJECT, implementation = User.class)
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
            responseCode = "409",
            description = "User already exists",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.OBJECT, implementation = ErrorResponse.class)
            )
    )
    public Response create(@NotNull @Valid UserPostResource userPostResource) {
        User user = userMapper.toDomain(userPostResource);

        User createdUser = userService.createUser(user);
        URI uri = UriBuilder.fromResource(UserResource.class).path("/user/" + createdUser.getId()).build();
        return Response.created(uri)
                .entity(createdUser)
                .build();
    }

    @DELETE
    @Path("user/{id}")
    @Operation(summary = "Delete a user specified with an id")
    @APIResponse(
            responseCode = "200",
            description = "Successfully deleted the user.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
    )
    @APIResponse(
            responseCode = "404",
            description = "The user to delete was not found.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
    )
    public Response delete(@PathParam("id") UUID id) {
        userService.deleteUser(id);
        return Response.ok().build();
    }
}

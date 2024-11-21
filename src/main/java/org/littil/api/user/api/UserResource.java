package org.littil.api.user.api;

import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.littil.api.auth.TokenHelper;
import org.littil.api.auth.service.AuthUser;
import org.littil.api.auth.service.AuthenticationService;
import org.littil.api.exception.ErrorResponse;
import org.littil.api.user.service.User;
import org.littil.api.user.service.UserMapper;
import org.littil.api.user.service.UserService;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
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
    TokenHelper tokenHelper;

    @Inject
    AuthenticationService authenticationService;

    @Inject
    UserDTOMapper userDTOMapper;


    @GET
    @Path("user")
    @RolesAllowed({"admin"})
    @Operation(summary = "Get all users")
    @APIResponse(
            responseCode = "200",
            description = "Get all users",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.ARRAY, implementation = UserDTO.class)
            )
    )
    public Response list() {
        List<AuthUser> authUsers = authenticationService.listAuthUsers();
        List<UserDTO> users = userService.listUsers()
                .stream()
                .map(userDTOMapper::toDTO)
                .map(user -> extendUserWithAuthUserDetails(user, authUsers) )
                .toList();
        return Response.ok(users).build();
    }

    private UserDTO extendUserWithAuthUserDetails(UserDTO user, List<AuthUser> authUsers) {
        Optional<AuthUser> authUser =  authUsers.stream()
                .filter(a -> a.getEmailAddress().equals(user.getEmailAddress()))
                .findFirst();
        return authUser.isEmpty() ? user : userDTOMapper.updateUserDTOFromAuthUser(authUser.get(), user);
    }

    @GET
    @Path("user/{id}")
    @RolesAllowed({"admin"})
    @Operation(summary = "Fetch a specific user by Id")
    @APIResponse(
            responseCode = "200",
            description = "User with Id found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = User.class)
            )
    )
    @APIResponse(
            responseCode = "404",
            description = "User with specific Id was not found."
    )
    public Response get(@Parameter(name = "id", required = true) @PathParam("id") final UUID id) {
        Optional<User> user = userService.getUserById(id);
        if (user.isPresent()) {
            return Response.ok(user).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Path("user/provider/{id}")
    @Authenticated
    @Operation(summary = "Fetch a specific user by provider Id")
    @APIResponse(
            responseCode = "200",
            description = "User with provider Id found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = User.class)
            )
    )
    @APIResponse(
            responseCode = "404",
            description = "User with specific provider Id was not found."
    )
    @APIResponse(
            responseCode = "403",
            description = "Access is not granted to retrieve this user."
    )
    public Response get(@Parameter(name = "id", required = true) @PathParam("id") final String providerId) {
        Optional<User> user = userService.getUserByProviderId(providerId);
        if (user.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        if(!tokenHelper.getCurrentUserId().equals(user.get().getId())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        return Response.ok(user).build();
    }

    @POST
    @Path("user")
    @Operation(summary = "Create a new user")
    @APIResponse(
            responseCode = "201",
            description = "User successfully created",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = User.class)
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
            description = "User with the same e-mail address already exists",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.OBJECT, implementation = ErrorResponse.class)
            )
    )
    public Response create(@NotNull @Valid UserPostResource userPostResource) {
        User user = userMapper.toDomain(userPostResource);
        User createdUser = userService.createUser(user);
        URI uri = UriBuilder.fromResource(UserResource.class).path("/user/" + createdUser.getId()).build();
        return Response.created(uri).entity(createdUser).build();
    }

    @DELETE
    @Path("user/{id}")
    @RolesAllowed({"admin"})
    @Operation(summary = "Delete a user specified with an id")
    @APIResponse(
            responseCode = "200",
            description = "Successfully deleted the user."
    )
    @APIResponse(
            responseCode = "404",
            description = "The user to delete was not found."
    )
    public Response delete(@PathParam("id") UUID id) {
        userService.deleteUser(id);
        return Response.ok().build();
    }
}
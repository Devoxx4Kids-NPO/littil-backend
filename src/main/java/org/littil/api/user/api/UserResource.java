package org.littil.api.user.api;

import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.littil.api.auth.TokenHelper;
import org.littil.api.exception.ErrorResponse;
import org.littil.api.user.service.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Path("/api/v2/users")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Users", description = "CRUD Operations for users")
public class UserResource {
    @Inject
    UserService userService;
    @Inject
    UserStatisticsService userStatisticsService;
    @Inject
    UserMapper userMapper;
    @Inject
    TokenHelper tokenHelper;

    @GET
    @RolesAllowed({"admin"})
    @Operation(summary = "Get all users")
    @APIResponse(
            responseCode = "200",
            description = "Get all users",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.ARRAY, implementation = User.class)
            )
    )
    public Response list() {
        List<User> users = userService.listUsers();
        return Response.ok(users).build();
    }

    @GET
    @Path("{id}")
    @RolesAllowed({"admin"})
    @Operation(summary = "Fetch a specific user by Id")
    @APIResponse(
            responseCode = "200",
            description = "User with Id found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.OBJECT, implementation = User.class)
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
    @Path("provider/{id}")
    @Authenticated
    @Operation(summary = "Fetch a specific user by provider Id")
    @APIResponse(
            responseCode = "200",
            description = "User with provider Id found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.OBJECT, implementation = User.class)
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
    @Path("{id}")
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

    @GET
    @Path("statistics")
    @RolesAllowed({"admin"})
    @Operation(summary = "Get statistics of users")
    @APIResponse(
            responseCode = "200",
            description = "Get statistics of users",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.ARRAY, implementation = UserStatistics.class)
            )
    )
    public Response getUserStatistics() {
        List<UserStatistics> userStatistics = userStatisticsService.getUserStatistics();
        return Response.ok(userStatistics).build();
    }

    @POST
    @Path("{id}/email-change/request")
    @RolesAllowed({"school", "guest_teacher"})
    @Operation(summary = "Send email with verification code to new email address")
    @APIResponse(
            responseCode = "204",
            description = "email send with verification code"
    )
    @APIResponse(
            responseCode = "401",
            description = "User is not authorized to send verification code for other user."
    )
    @APIResponse(
            responseCode = "409",
            description = "Email address already in use."
    )
    @APIResponse(
            responseCode = "409",
            description = "Verification process still in progress. Please wait before requesting a new code."
    )
    public Response sendEmailWithVerificationCode(@Parameter(name = "id", required = true) @PathParam("id")final UUID id,
                          @NotNull EmailVerficationCodeResource emailVerificationResource)
    {
        if ( ! tokenHelper.getCurrentUserId().equals(id) ) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        String emailAddress = emailVerificationResource.getEmailAddress();
    	userService.sendVerificationCode(id, emailAddress);
        return Response.noContent().build();
    }

    @PATCH
    @Path("{id}/email-change/verify")
    @RolesAllowed({"school", "guest_teacher"})
    @Operation(summary = "Change email address upon successful verification")
    @APIResponse(
            responseCode = "204",
            description = "Email changed successfully"
    )
    @APIResponse(
            responseCode = "401",
            description = "User is not authorized to change email for other user."
    )
    @APIResponse(
            responseCode = "409",
            description = "Email address already in use."
    )
    @APIResponse(
            responseCode = "409",
            description = "Verification code is missing or expired"
    )
    public Response changeEmail(@Parameter(name = "id", required = true) @PathParam("id")final UUID id,
                                @NotNull ChangeEmailResource changeEmailResource) {
        if ( ! tokenHelper.getCurrentUserId().equals(id) ) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        userService.changeEmail(id, changeEmailResource);
        return Response.noContent().build();
    }

}
package org.littil.api.auth;

import java.net.URI;
import java.util.List;

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

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.littil.api.exception.ErrorResponse;

// Temporary ## Added to test functionality of interface with auth0.com

@Path("/api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Auth", description = "CRUD Operations")
public class authResource {

	@Inject
	AuthenticationService authenticationService;

	@GET
	@Path("user")
	@Operation(summary = "Get all users")
	@APIResponse(responseCode = "200", description = "Get all users", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.ARRAY, implementation = User.class)))
	public Response list() {
		List<User> users = authenticationService.listUsers();
		return Response.ok(users).build();
	}

	@GET
	@Path("user/{id}")
	@Operation(summary = "Fetch a specific user by Id")
	@APIResponse(responseCode = "200", description = "User with Id found.", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.OBJECT, implementation = User.class)))
	@APIResponse(responseCode = "404", description = "User with specific Id was not found.", content = @Content(mediaType = MediaType.APPLICATION_JSON))
	public Response get(@Parameter(name = "id", required = true) @PathParam("id") final String id) {
		User user = authenticationService.getUserById(id);

		return Response.ok(user).build();
	}

	@GET
	@Path("/user/email/{email}")
	@Operation(summary = "Fetch user by email")
	@APIResponse(responseCode = "200", description = "User with email found.", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.ARRAY, implementation = User.class)))
	public Response getByEmail(@Parameter(name = "email", required = true) @PathParam("email") final String email) {
		User user = authenticationService.getUserByEmail(email);

		return Response.ok(user).build();
	}

	@POST
	@Path("user")
	@Operation(summary = "Create a new user")
	@APIResponse(responseCode = "201", description = "User successfully created", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.OBJECT, implementation = User.class)))
	@APIResponse(responseCode = "400", description = "Validation errors occurred.", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.OBJECT, implementation = ErrorResponse.class)))
	public Response create(@NotNull @Valid User user) {
		User createdUser = authenticationService.createUser(user);
		URI uri = UriBuilder.fromResource(authResource.class).path("/" + createdUser.getId()).build();
		return Response.created(uri).entity(createdUser).build();
	}

	@DELETE
	@Path("user/{id}")
	@Operation(summary = "Delete a user specified with an Id")
	@APIResponse(responseCode = "200", description = "Successfully deleted the user.", content = @Content(mediaType = MediaType.APPLICATION_JSON))
	@APIResponse( // TODO
			responseCode = "404", description = "The user to delete was not found.", content = @Content(mediaType = MediaType.APPLICATION_JSON))
	public Response delete(@PathParam("id") String id) {
		authenticationService.deleteUser(id);
		return Response.ok().build();
	}

	@GET
	@Path("role")
	@Operation(summary = "Get all roles")
	@APIResponse(responseCode = "200", description = "Get all roles", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.ARRAY, implementation = User.class)))
	public Response listRoles() {
		List<RoleEntity> roles = authenticationService.getRoles();
		return Response.ok(roles).build();
	}

}

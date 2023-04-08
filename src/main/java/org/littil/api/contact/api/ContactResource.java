package org.littil.api.contact.api;

import io.quarkus.security.Authenticated;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.littil.api.auth.TokenHelper;
import org.littil.api.auth.authz.SchoolSecured;
import org.littil.api.contact.service.Contact;
import org.littil.api.contact.service.ContactMapper;
import org.littil.api.contact.service.ContactService;
import org.littil.api.exception.ErrorResponse;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Path("/api/v1/contacts")
@RequestScoped
@Slf4j
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@SchoolSecured
@Tag(name = "Contact", description = "CRUD Operations")
public class ContactResource {

    @Inject
    ContactService contactService;
    @Inject
    ContactMapper contactMapper;

    @GET
    @Operation(summary = "Get all my contacts")
    @APIResponse(
            responseCode = "200",
            description = "Get all my contacts",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.ARRAY, implementation = Contact.class)
            )
    )
    public Response list() {
        List<Contact> contacts = contactService.findAll();
        return Response.ok(contacts).build();
    }

    @GET
    @Path("{id}")
    @Operation(summary = "Fetch a specific contact by Id")
    @APIResponse(
            responseCode = "200",
            description = "Contact with Id found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.OBJECT, implementation = Contact.class)
            )
    )
    @APIResponse(
            responseCode = "404",
            description = "Contact with specific Id was not found."
    )
    public Response get(@Parameter(name = "id", required = true) @PathParam("id") final UUID id) {
        Optional<Contact> contact = contactService.getContactBy(id);
        return contact.map(r -> Response.ok(r).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    @Operation(summary = "Create or update a contact")
    @APIResponse(
            responseCode = "200",
            description = "Contact successfully created or updated",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.OBJECT, implementation = Contact.class)
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
            responseCode = "500",
            description = "Persistence error occurred. Failed to persist contact."
    )
    public Response createOrUpdate(@NotNull @Valid ContactPostResource contact) {
        Contact persistedContact = contactService.sendAndSave(contactMapper.toDomain(contact));
        URI uri = UriBuilder.fromResource(ContactResource.class)
                .path("/" + persistedContact.getId()).build();
        return Response.ok(uri).entity(persistedContact).build();
    }
}

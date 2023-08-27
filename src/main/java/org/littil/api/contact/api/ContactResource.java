package org.littil.api.contact.api;

import io.quarkus.security.Authenticated;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
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
import java.util.List;

@Path("/api/v1/contacts")
@RequestScoped
@Slf4j
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
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
                    schema = @Schema(type = SchemaType.ARRAY, implementation = ContactResponse.class)
            )
    )
    @APIResponse(
            responseCode = "401",
            description = "Not authenticated"
    )
    public Response list() {
        List<ContactResponse> contacts = contactService.findAllMyContacts()
                .stream()
                .map(contactMapper::toResponse)
                .toList();
        return Response.ok(contacts).build();
    }

    @POST
    @Operation(summary = "Send and save a contact")
    @APIResponse(
            responseCode = "200",
            description = "Contact successfully sent and created",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.OBJECT, implementation = ContactResponse.class)
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
            responseCode = "401",
            description = "Not authenticated"
    )
    @APIResponse(
            responseCode = "500",
            description = "error occurred. Failed to send or persist contact."
    )
    public Response sendAndSave(@NotNull @Valid ContactPostResource contact) {
        return contactService.saveAndSend(contactMapper.toDomain(contact))
                .map(contactMapper::toResponse)
                .map(Response::ok)
                .orElseGet(() -> Response.status(Response.Status.INTERNAL_SERVER_ERROR))
                .build();
    }
}

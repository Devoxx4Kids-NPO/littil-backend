package org.littil.api.mail;

import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.littil.api.auth.TokenHelper;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/mail")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@Tag(name = "mail", description = "E-Mail Operations")
public class mailResource {

    @Inject
    MailService mailService;
    @Inject
    TokenHelper tokenHelper;


    @POST
    @Path("/verifiy")
    @RolesAllowed({"admin"})  // TODO school / guestTeacher
    @Operation(summary = "Send email with verification code for register/change email address")
    @APIResponse(
            responseCode = "200",
            description = "email send with verification code",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON //,
//                    schema = @Schema(type = SchemaType.OBJECT, implementation = GuestTeacher.class)
            )
    )
//    @APIResponse(
//            responseCode = "400",
//            description = "Validation errors occurred.",
//            content = @Content(
//                    mediaType = MediaType.APPLICATION_JSON,
//                    schema = @Schema(type = SchemaType.OBJECT, implementation = ErrorResponse.class)
//            )
//    )
//    @APIResponse(
//            responseCode = "404",
//            description = "No Teacher found for id provided"
//    )
//    @APIResponse(
//            responseCode = "409",
//            description = "Current user already has authorizations"
//    )
//    @APIResponse(
//            responseCode = "500",
//            description = "Persistence error occurred. Failed to persist teacher.",
//            content = @Content(mediaType = MediaType.APPLICATION_JSON)
//    )
    public Response sendEmailWithVerificationCode(@NotNull EmailVerficationResource emailVerificationResource)
    {
    	String emailAddress = emailVerificationResource.getEmailAddress();     	
    	mailService.sendVerificationCode(emailAddress);
        return Response.ok().build();
    }

}

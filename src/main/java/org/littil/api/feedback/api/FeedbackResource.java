package org.littil.api.feedback.api;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.littil.api.mail.MailService;

@Path("/api/v1/feedback")
@RequestScoped
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Feedback")
public class FeedbackResource {

    @Inject
    MailService mailService;
    @Inject
    @ConfigProperty(name = "org.littil.feedback.email")
    String feedbackEmail;

    @POST
    @APIResponse(
            responseCode = "200",
            description = "Receive feedback and forward to mail",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = FeedbackPostResource.class)
            )
    )
    public Response feedback(@NotNull @Valid FeedbackPostResource feedback) {
        mailService.sendFeedbackMail(feedback, feedbackEmail);
        return Response.ok().build();
    }
}

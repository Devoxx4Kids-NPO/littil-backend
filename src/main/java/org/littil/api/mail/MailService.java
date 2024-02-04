package org.littil.api.mail;

import io.quarkus.mailer.MailTemplate;
import io.quarkus.mailer.MailTemplate.MailTemplateInstance;
import io.quarkus.qute.CheckedTemplate;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.littil.api.user.service.User;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Slf4j
@ApplicationScoped
public class MailService {

    @Inject
    @ConfigProperty(name = "org.littil.feedback.email")
    Optional<String> feedbackEmail;

    @CheckedTemplate
    static class Templates {
        public static native MailTemplate.MailTemplateInstance welcome(String userEmail, String temporaryPassword);
        public static native MailTemplate.MailTemplateInstance contactRecipient(String contactMessage, String contactMedium);
        public static native MailTemplate.MailTemplateInstance contactInitiatingUser(String contactMessage, String contactMedium);
        public static native MailTemplate.MailTemplateInstance feedback(String feedbackType, String message);
    }

    public void sendWelcomeMail(User user, String password) {
        send(Templates.welcome(user.getEmailAddress(), password)
                        .to(user.getEmailAddress())
                        .subject("Welkom bij Littil"));
    }

    public void sendContactMailRecipient(String emailAddress, String contactMessage, String contactMedium, String cc) {
        var template = Templates.contactRecipient(contactMessage,contactMedium);
        sendContactMail(template, emailAddress, contactMessage, contactMedium, cc);
    }

    public void sendContactMailInitiatingUser(String emailAddress, String contactMessage, String contactMedium, String cc) {
        var template = Templates.contactInitiatingUser(contactMessage, contactMedium);
        sendContactMail(template, emailAddress, contactMessage, contactMedium, cc);
    }

    private void sendContactMail(MailTemplateInstance template, String emailAddress, String contactMessage, String contactMedium, String cc) {
        template.to(emailAddress)
            .subject("Contactverzoek voor Littil");
        if(cc !=null) {
            template = template.cc(cc);
        }
        send(template);
    }

    public void sendFeedbackMail(String feedbackType, String feedbackMessage) {
        if (feedbackEmail.isEmpty()) {
            log.warn("sending email with feedback is skipped. type: {}, message: {}",
                    feedbackType, feedbackMessage);
            return;
        }
        var template = Templates.feedback(feedbackType, feedbackMessage)
                .to(feedbackEmail.get())
                .subject("Feedback ontvangen");
        send(template);
    }

    private void send(MailTemplate.MailTemplateInstance template) {
        log.info("sending {}",template);
        try {
            template
                    .send()
                    .subscribe()
                    .asCompletionStage()
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            log.warn("sending {} failed",template, e);
            throw new RuntimeException(e);
        }
    }
}

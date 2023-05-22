package org.littil.api.mail;

import io.quarkus.mailer.MailTemplate;
import io.quarkus.qute.CheckedTemplate;
import lombok.extern.slf4j.Slf4j;
import org.littil.api.user.service.User;

import javax.enterprise.context.ApplicationScoped;
import java.util.concurrent.ExecutionException;

@Slf4j
@ApplicationScoped
public class MailService {
    @CheckedTemplate
    static class Templates {
        public static native MailTemplate.MailTemplateInstance welcome(String userEmail, String temporaryPassword);
        public static native MailTemplate.MailTemplateInstance contact(String contactMessage, String contactMedium);
    }

    public void sendWelcomeMail(User user, String password) {
        send(Templates.welcome(user.getEmailAddress(), password)
                        .to(user.getEmailAddress())
                        .subject("Welkom bij Littil"));
    }

    public void sendContactMail(String recipient, String contactMessage, String contactMedium, String cc) {
        var template = Templates.contact(contactMessage,contactMedium)
                .to(recipient)
                .subject("Contactverzoek voor Littil");
        if(cc!=null) {
            template = template.cc(cc);
        }
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

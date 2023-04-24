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
        send(Templates.welcome(user.getEmailAddress(), password),user.getEmailAddress(),"Welkom bij Littil");
    }

    public void sendContactMail(String recipient, String contactMessage, String contactMedium) {
        send(Templates.contact(contactMessage,contactMedium),recipient,"Contactverzoek voor Littil");
    }

    private void send(MailTemplate.MailTemplateInstance template,String recipient,String subject) {
        log.info("sending {} to {}",template, recipient);
        try {
            template
                    .to(recipient)
                    .subject(subject)//todo make subject configurable?
                    .send()
                    .subscribe()
                    .asCompletionStage()
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            //todo fix handling
            log.warn("mailing {} to {} failed ",template,recipient, e);
            throw new RuntimeException(e);
        }
    }
}

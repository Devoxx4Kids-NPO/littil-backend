package org.littil.api.mail;

import io.quarkus.mailer.MailTemplate;
import io.quarkus.qute.CheckedTemplate;
import io.smallrye.mutiny.groups.UniSubscribe;
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
    }

    public void sendWelcomeMail(User user, String password) {
        log.info("sending mail to {}", user.getEmailAddress());
        //todo now sending only NL mails
        UniSubscribe<Void> uni = Templates.welcome(user.getEmailAddress(), password)
                .to(user.getEmailAddress())
                .subject("Welkom bij Littil") //todo make subject configurable?
                .send().subscribe();
        //todo fix handling
        try {
            uni.asCompletionStage().get();
        } catch (InterruptedException | ExecutionException e) {
            log.info("mailing failed! ", e);
            throw new RuntimeException(e);
        }
    }
}

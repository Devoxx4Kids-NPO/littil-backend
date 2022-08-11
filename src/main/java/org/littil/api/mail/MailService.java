package org.littil.api.mail;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import org.littil.api.user.service.User;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class MailService {

    @Inject
    Mailer mailer;

    public void sendWelcomeMail(User user, String password) {
        // todo do mail things
        Mail mail = new Mail();
        mail.addTo(user.getEmailAddress());
        mail.setSubject("somesubject");

        mailer.send(mail);
    }
}

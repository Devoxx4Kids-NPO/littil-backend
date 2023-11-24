package org.littil.api.mail;

import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.ext.mail.MailMessage;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.littil.TestFactory;
import org.littil.api.user.service.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class MailServiceTest {
    @Inject
    MockMailbox mailbox;
    @Inject
    MailService mailService;

    @BeforeEach
    void init() {
        mailbox.clear();
    }

    @ParameterizedTest
    @CsvSource({
            "welcome-mail-1@littil.org,testpassword",
            "welcome-mail-2@littil.org,pindakaasmetworst"
    })
    void testSendWelcomeMail(String email, String password) {
        User user = TestFactory.createUser();
        user.setEmailAddress(email);

        // sut
        mailService.sendWelcomeMail(user, password);

        // verify
        MailMessage sent = mailbox.getMailMessagesSentTo(email).stream().findFirst().get();
        assertEquals("Welkom bij Littil", sent.getSubject());
        assertTrue(sent.getText().contains(password));
    }

    @ParameterizedTest
    @CsvSource({
            "contact-mail-1@littil.org,bericht-1,+31613371337",
            "contact-mail-2@littil.org,bericht-2,sender-mail@littil.org"
    })
    void testSendContactMail(String email, String message, String medium) {
        // sut
        mailService.sendContactMail(email, message, medium, null);

        // verify
        MailMessage sent = mailbox.getMailMessagesSentTo(email).stream().findFirst().get();
        assertEquals("Contactverzoek voor Littil", sent.getSubject());
        assertTrue(sent.getText().contains(message));
        assertTrue(sent.getText().contains(medium));
    }

    @Test
    void testSendContactMailWithCc() {
        // sut
        mailService.sendContactMail("contact-mail@littil.org", "bericht-met-cc", "+31613371337", "cc@littil.org");

        // verify
        assertEquals(2, mailbox.getTotalMessagesSent());
        assertEquals(1, mailbox.getMailMessagesSentTo("contact-mail@littil.org").size());
        assertEquals(1, mailbox.getMailMessagesSentTo("cc@littil.org").size());
    }

    @ParameterizedTest
    @CsvSource({
            "issue,something is wrong,feedback@littil.org",
            "idea,just some idea,feedback@littil.org"
    })
    void testSendFeedbackMail(String feedbackType, String message, String email) {
        // sut
        mailService.sendFeedbackMail(feedbackType, message);

        // verify
        MailMessage sent = mailbox.getMailMessagesSentTo(email).stream().findFirst().get();
        assertEquals("Feedback ontvangen", sent.getSubject());
        assertTrue(sent.getText().contains(feedbackType));
        assertTrue(sent.getText().contains(message));
    }

    @Test
    void testSendFeedbackMailWithFeedbackEmailOptionalEmpty() {
        // sut
        mailService.feedbackEmail = Optional.empty();
        final var feedbackType = "feedbackType";
        final var message = "message";

        // verify
        mailService.sendFeedbackMail(feedbackType, message);
        assertEquals(0 , mailbox.getTotalMessagesSent());
    }

}
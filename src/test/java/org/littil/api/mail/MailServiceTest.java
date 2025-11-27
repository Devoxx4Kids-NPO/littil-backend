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
import org.littil.api.user.service.VerificationCode;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class MailServiceTest {
    @Inject
    MockMailbox mailbox;

    MailService mailService;

    @BeforeEach
    void init() {
        mailService = new MailService();
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
        Optional<MailMessage> sent = mailbox.getMailMessagesSentTo(email).stream().findFirst();
        assertTrue(sent.isPresent());
        assertEquals("Welkom bij Littil", sent.get().getSubject());
        assertTrue(sent.get().getText().contains(password));
    }

    @ParameterizedTest
    @CsvSource({
            "contact-mail-1@littil.org,bericht-1,+31613371337",
            "contact-mail-2@littil.org,bericht-2,sender-mail@littil.org"
    })
    void testSendContactMailRecipient(String email, String message, String medium) {
        // sut
        mailService.sendContactMailRecipient(email, message, medium, null);

        // verify
        Optional<MailMessage> sent = mailbox.getMailMessagesSentTo(email).stream().findFirst();
        assertTrue(sent.isPresent());
        assertEquals("Contactverzoek voor Littil", sent.get().getSubject());
        assertTrue(sent.get().getText().contains(message));
        assertTrue(sent.get().getText().contains(medium));
    }

    @Test
    void testSendContactMailRecipientWithCc() {
        // sut
        mailService.sendContactMailRecipient("contact-mail@littil.org", "bericht-met-cc", "+31613371337", "cc@littil.org");

        // verify
        assertEquals(2, mailbox.getTotalMessagesSent());
        assertEquals(1, mailbox.getMailMessagesSentTo("contact-mail@littil.org").size());
        assertEquals(1, mailbox.getMailMessagesSentTo("cc@littil.org").size());
    }

    @ParameterizedTest
    @CsvSource({
        "contact-mail-1@littil.org,bericht-1,+31613371337",
        "contact-mail-2@littil.org,bericht-2,sender-mail@littil.org"
    })
    void testSendContactMailInitiatingUser(String email, String message, String medium) {
        // sut
        mailService.sendContactMailInitiatingUser(email, message, medium, null);

        // verify
        Optional<MailMessage> sent = mailbox.getMailMessagesSentTo(email).stream().findFirst();
        assertTrue(sent.isPresent());
        assertEquals("Contactverzoek voor Littil", sent.get().getSubject());
        assertTrue(sent.get().getText().contains(message));
        assertTrue(sent.get().getText().contains(medium));
    }

    @Test
    void testSendContactMailInitiatingUserwithCc() {
        // sut
        mailService.sendContactMailInitiatingUser("contact-mail@littil.org", "bericht-met-cc", "+31613371337", "cc@littil.org");

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
        mailService.feedbackEmail = Optional.of(email);
        mailService.sendFeedbackMail(feedbackType, message);

        // verify
        Optional<MailMessage> sent = mailbox.getMailMessagesSentTo(email).stream().findFirst();
        assertTrue(sent.isPresent());
        assertEquals("Feedback ontvangen", sent.get().getSubject());
        assertTrue(sent.get().getText().contains(feedbackType));
        assertTrue(sent.get().getText().contains(message));
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

    @Test
    void testSendVerificationCodeMail() {
        User user = TestFactory.createUser();
        String email = user.getEmailAddress();
        VerificationCode verificationCode = new VerificationCode(user.getId(),email);

        // sut
        mailService.sendVerificationCode(verificationCode);

        // verify
        assertEquals(1 , mailbox.getTotalMessagesSent());
        Optional<MailMessage> sent = mailbox.getMailMessagesSentTo(email).stream().findFirst();
        assertTrue(sent.isPresent());
        assertEquals("LiTTiL email verificatie code", sent.get().getSubject());
        assertEquals(1, sent.get().getTo().size());
        assertEquals(email, sent.get().getTo().get(0));
        assertTrue(sent.get().getText().contains(verificationCode.getToken()));
    }
}
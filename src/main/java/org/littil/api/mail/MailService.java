package org.littil.api.mail;

import io.quarkus.mailer.MailTemplate;
import io.quarkus.mailer.MailTemplate.MailTemplateInstance;
import io.quarkus.qute.CheckedTemplate;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.littil.api.user.service.User;

import jakarta.enterprise.context.ApplicationScoped;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
public class MailService {
	
	public static Map<String, VerificationCodeDetails> verificationCodeMap = new HashMap<>();

    @Inject
    @ConfigProperty(name = "org.littil.feedback.email")
    Optional<String> feedbackEmail;

    @CheckedTemplate
    static class Templates {
        public static native MailTemplate.MailTemplateInstance welcome(String userEmail, String temporaryPassword);
        public static native MailTemplate.MailTemplateInstance contactRecipient(String contactMessage, String contactMedium);
        public static native MailTemplate.MailTemplateInstance contactInitiatingUser(String contactMessage, String contactMedium);
        public static native MailTemplate.MailTemplateInstance feedback(String feedbackType, String message);
        public static native MailTemplate.MailTemplateInstance verificationcode(String verificationCode);
    }

    public void sendWelcomeMail(User user, String password) {
        send(Templates.welcome(user.getEmailAddress(), password)
                        .to(user.getEmailAddress())
                        .subject("Welkom bij Littil"));
    }

    public void sendContactMailRecipient(String emailAddress, String contactMessage, String contactMedium, String cc) {
        var template = Templates.contactRecipient(contactMessage,contactMedium);
        sendContactMail(template, emailAddress, cc);
    }

    public void sendContactMailInitiatingUser(String emailAddress, String contactMessage, String contactMedium, String cc) {
        var template = Templates.contactInitiatingUser(contactMessage, contactMedium);
        sendContactMail(template, emailAddress, cc);
    }

    private void sendContactMail(MailTemplateInstance template, String emailAddress, String cc) {
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

	public void sendVerificationCode(String newEmailAddress) {
		String verificationCode = getVerificationCode(newEmailAddress);
		var template = Templates.verificationcode(verificationCode)
				.to(newEmailAddress)
				.subject("LiTTiL email verificatie code");
        send(template);
	}
	
	public boolean verifyVerificationCode(String emailAddress, String verificationCode) {
		cleanVerificationCodeMap();
		if (verificationCodeMap.containsKey(emailAddress)) {
			throw new IllegalArgumentException("Verification code not found");   // TODO correct ExceptionType ?
		}
		return verificationCodeMap.get(emailAddress).getVerificationCode().equals(verificationCode);
	}
	
	private String getVerificationCode(String emailAddress) {
		cleanVerificationCodeMap();
		// validate e-mailaddress		
		if (verificationCodeMap.containsKey(emailAddress)) {
			throw new IllegalArgumentException("Verification process still in progress");  // TODO correct ExceptionType ?
		}
		// create verification code and store
		VerificationCodeDetails verificationCodeDetails = new VerificationCodeDetails(emailAddress);
		verificationCodeMap.put(emailAddress, verificationCodeDetails);
		return  verificationCodeDetails.getVerificationCode();
	}
	
	private void cleanVerificationCodeMap() {
		// remove expired e-mailaddresses from map
		verificationCodeMap = verificationCodeMap.entrySet().stream() //
				.filter(entry -> entry.getValue().getExpireTime() <= System.currentTimeMillis())
			    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
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
    
    @Getter
    class VerificationCodeDetails {
    	final String emailAddress;
    	final String verificationCode;
    	final Long expireTime;
    	
    	public VerificationCodeDetails(String emailAddress) {
    		this.emailAddress = emailAddress;
    		this.verificationCode = "123-456";
    		this.expireTime = System.currentTimeMillis() + 5 * 60 * 1000; // TODO constant + mention in mail)
    	}
    }

}

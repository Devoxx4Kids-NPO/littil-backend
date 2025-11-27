package org.littil.api.user.service;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import org.littil.api.exception.VerificationCodeException;

/**
 * Service class responsible for generating and validating email verification codes.
 */
@ApplicationScoped
public class VerificationCodeService {


    /** Stores verification code details mapped by email address */
    private Map<UUID, VerificationCode> verificationCodeMap = new HashMap<>();


    /**
     * Validates the provided token code for the given email address.
     *
     * @param userId the userId how requeted the verificationCode
     * @param emailAddress the email address associated with the verification code
     * @param token the verification code to validate
     * @return true if the code is valid and matches; false otherwise
     * @throws NoSuchElementException if no verification code exists for the email address
     */
    public boolean isValidToken(UUID userId, String emailAddress, String token) {
        cleanVerificationCodeMap();
        if (!verificationCodeMap.containsKey(userId)) {
            throw new VerificationCodeException("Verification code is missing or expired");
        }
        VerificationCode verificationCode = verificationCodeMap.get(userId);
        verificationCodeMap.remove(userId);
        return verificationCode.getEmailAddress().equals(emailAddress)  &&
             verificationCode.getToken().equals(token);
    }

    /**
     * Generates a new verification code for the given email address.
     * If a code is already in progress, an exception is thrown.
     *
     * @param emailAddress the email address to generate a code for
     * @return the newly generated verification code
     * @throws IllegalStateException if a verification process is already in progress
     */
    public VerificationCode getVerificationCode(UUID userId, String emailAddress) {
        cleanVerificationCodeMap();
        if (verificationCodeMap.containsKey(userId)) {
            throw new VerificationCodeException("Verification process still in progress");
        }
        VerificationCode verificationCode = new VerificationCode(userId, emailAddress);
        verificationCodeMap.put(userId, verificationCode);
        return verificationCode;
    }

    /**
     * Removes expired verification codes from the map.
     */
    private void cleanVerificationCodeMap() {
        verificationCodeMap = verificationCodeMap.entrySet().stream()
                .filter( entry -> !entry.getValue().isExpired())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}

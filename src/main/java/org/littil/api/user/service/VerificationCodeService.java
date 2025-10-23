package org.littil.api.user.service;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Service class responsible for generating and validating email verification codes.
 */
@ApplicationScoped
public class VerificationCodeService {


    /** Stores verification code details mapped by email address */
    private Map<String, VerificationCode> verificationCodeMap = new HashMap<>();


    /**
     * Validates the provided token code for the given email address.
     *
     * @param emailAddress     the email address associated with the verification code
     * @param token the verification code to validate
     * @return true if the code is valid and matches; false otherwise
     * @throws NoSuchElementException if no verification code exists for the email address
     */
    public boolean isValidToken(String emailAddress, String token) {
        cleanVerificationCodeMap();
        if (!verificationCodeMap.containsKey(emailAddress)) {
            throw new NoSuchElementException("Verification code is missing or expired");  // TODO final Exception ?
        }
        return verificationCodeMap.get(emailAddress).getToken().equals(token);
    }

    /**
     * Generates a new verification code for the given email address.
     * If a code is already in progress, an exception is thrown.
     *
     * @param emailAddress the email address to generate a code for
     * @return the newly generated verification code
     * @throws IllegalStateException if a verification process is already in progress
     */
    public VerificationCode getVerificationCode(String emailAddress) {
        cleanVerificationCodeMap();
        if (verificationCodeMap.containsKey(emailAddress)) {
            throw new IllegalStateException("Verification process still in progress");
        }
        VerificationCode verificationCode = new VerificationCode(emailAddress);
        verificationCodeMap.put(emailAddress, verificationCode);
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

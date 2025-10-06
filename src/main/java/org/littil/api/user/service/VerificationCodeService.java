package org.littil.api.user.service;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;



import java.util.concurrent.ThreadLocalRandom;

/**
 * Service class responsible for generating and validating email verification codes.
 */
@ApplicationScoped
public class VerificationCodeService {

    /** Stores verification code details mapped by email address */
    private Map<String, VerificationCodeDetails> verificationCodeMap = new HashMap<>();

    /** Expiration time for verification codes in milliseconds (5 minutes) */
    private static final long VERIFICATION_CODE_EXPIRATION_MS = 5 * 60 * 1_000L;

    //TODO all methods static / hide constructor
    
    /**
     * Validates the provided verification code for the given email address.
     *
     * @param emailAddress     the email address associated with the verification code
     * @param verificationCode the verification code to validate
     * @return true if the code is valid and matches; false otherwise
     * @throws NoSuchElementException if no verification code exists for the email address
     */
    public boolean isValidVerificationCode(String emailAddress, String verificationCode) {
        cleanVerificationCodeMap();
        if (!verificationCodeMap.containsKey(emailAddress)) {
            throw new NoSuchElementException("Verification code not found");  // TODO final Exception ?
        }
        return verificationCodeMap.get(emailAddress).getVerificationCode().equals(verificationCode);
    }

    /**
     * Generates a new verification code for the given email address.
     * If a code is already in progress, an exception is thrown.
     *
     * @param emailAddress the email address to generate a code for
     * @return the newly generated verification code
     * @throws IllegalStateException if a verification process is already in progress
     */
    public String getVerificationCode(String emailAddress) {
        cleanVerificationCodeMap();
        if (verificationCodeMap.containsKey(emailAddress)) {
            throw new IllegalStateException("Verification process still in progress");
        }
        VerificationCodeDetails verificationCodeDetails = new VerificationCodeDetails(emailAddress);
        verificationCodeMap.put(emailAddress, verificationCodeDetails);
        // TODO: Mention VERIFICATION_CODE_EXPIRATION_MS in the email sent to the user
        return verificationCodeDetails.getVerificationCode();
    }

    /**
     * Removes expired verification codes from the map.
     */
    private void cleanVerificationCodeMap() {
        verificationCodeMap = verificationCodeMap.entrySet().stream()
                .filter(entry -> entry.getValue().getExpireTime() > System.currentTimeMillis())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Inner class representing the details of a verification code.
     */
    @Getter
    class VerificationCodeDetails {
        final String emailAddress;
        final String verificationCode;
        final Long expireTime;

        /**
         * Constructs a new VerificationCodeDetails instance for the given email address.
         * The verification code is randomly generated and expires after a fixed duration.
         *
         * @param emailAddress the email address to associate with the verification code
         */
        public VerificationCodeDetails(String emailAddress) {
            this.emailAddress = emailAddress;
            this.verificationCode = generateRandomCode();
            this.expireTime = System.currentTimeMillis() + VERIFICATION_CODE_EXPIRATION_MS;
        }

        /**
         * Generates a random 6-digit verification code in the format "XXX-XXX".
         *
         * @return a randomly generated verification code
         */
        private String generateRandomCode() {
            int part1 = ThreadLocalRandom.current().nextInt(100, 1000);
            int part2 = ThreadLocalRandom.current().nextInt(100, 1000);
            return String.format("%03d-%03d", part1, part2);
        }
    }
}

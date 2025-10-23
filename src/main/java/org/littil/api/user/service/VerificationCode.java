package org.littil.api.user.service;

import lombok.AccessLevel;
import lombok.Getter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.security.SecureRandom;

/**
 * Class representing the details of an email verification code.
 */

@Getter
public class VerificationCode {

    @Schema(description = "Email address associated with the verification code", example = "user@example.com")

    final String emailAddress;

    @Schema(description = "The token sent to the user", example = "AB1-C23")
    final String token;

    @Schema(description = "Expiration time in epoch milliseconds (internal use only)", hidden = true)
    @Getter(AccessLevel.NONE)
    private final Long expireTime;

    @Schema(description = "Duration in seconds for which the code is valid", example = "900")
    final int expiresIn;

    /** Expire time for verification codes in milliseconds (5 minutes) */
    private static final int VERIFICATION_CODE_EXPIRATION_SECONDS = 5 * 60;

    /** Character pool for generating random verification codes */
    private static final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    /**
     * Constructs a new VerificationCodeDetails instance for the given email address.
     * The verification code is randomly generated and expires after a fixed duration.
     *
     * @param emailAddress the email address to associate with the verification code
     */
    public VerificationCode(String emailAddress) {
        this.emailAddress = emailAddress;
        this.token = generateRandomCode();
        this.expiresIn = VERIFICATION_CODE_EXPIRATION_SECONDS;
        this.expireTime = System.currentTimeMillis() + VERIFICATION_CODE_EXPIRATION_SECONDS * 1000L;
    }

    /**
     * Checks if the verification code has expired.
     *
     * @return true if the code has expired; false otherwise
     */
    public boolean isExpired() {
        return System.currentTimeMillis() > expireTime;
    }

    /**
     * Generates a random 6-digit verification code in the format "XXX-XXX".
     *
     * @return a randomly generated verification code
     */
    private String generateRandomCode() {
        SecureRandom secureRandom = new SecureRandom();
        return generateRandomString(secureRandom) + "-" + generateRandomString(secureRandom);
    }

    private static String generateRandomString(SecureRandom secureRandom) {
        int length = 3;
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = secureRandom.nextInt(CHAR_POOL.length());
            sb.append(CHAR_POOL.charAt(index));
        }
        return sb.toString();
    }

}

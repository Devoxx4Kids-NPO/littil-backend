package org.littil.api.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import org.littil.api.exception.VerificationCodeException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class VerificationCodeServiceTest {

    private VerificationCodeService service;

    @BeforeEach
    void setUp() {
        service = new VerificationCodeService();
    }

    @Test
    void GetVerificationCode_Success() {
        UUID userId = UUID.randomUUID();
        String email = "user1@example.com";
        VerificationCode verificationCode = service.getVerificationCode(userId, email);

        assertNotNull(verificationCode);
        assertNotNull(verificationCode.getToken());
        assertTrue(verificationCode.getToken().matches("[A-Z0-9]{3}-[A-Z0-9]{3}"));
    }

    @Test
    void testGetVerificationCode_AlreadyExists_ThrowsException() {
        UUID userId = UUID.randomUUID();
        String email = "user2@example.com";
        service.getVerificationCode(userId, email);

        assertThrows(VerificationCodeException.class, () -> service.getVerificationCode(userId, email));
    }

    @Test
    void testIsValidToken_Success() {
        UUID userId = UUID.randomUUID();
        String email = "user3@example.com";
        VerificationCode code = service.getVerificationCode(userId, email);

        assertTrue(service.isValidToken(userId, email, code.getToken()));

        // can not validate token twice
        assertThrows(VerificationCodeException.class, () ->
                service.isValidToken(userId, email, code.getToken()));
    }

    @Test
    void testIsValidTokenWithIncorrectToken_ReturnsFalse() {
        UUID userId = UUID.randomUUID();
        String email = "user4@example.com";
        service.getVerificationCode(userId, email);

        assertFalse(service.isValidToken(userId, email, "000-000"));
    }

    @Test
    void testIsValidTokenWithIncorrectEmail_ReturnsFalse() {
        UUID userId = UUID.randomUUID();
        String email = "user4@example.com";
        VerificationCode verificationCode = service.getVerificationCode(userId, email);

        assertFalse(service.isValidToken(userId, "other-email@example.com", verificationCode.getToken()));
    }

    @Test
    void testIsValidToken_ThrowsException() {
        UUID userId = UUID.randomUUID();
        assertThrows(VerificationCodeException.class, () ->
                service.isValidToken(userId,"missing@example.com", "123-456"));
    }
}

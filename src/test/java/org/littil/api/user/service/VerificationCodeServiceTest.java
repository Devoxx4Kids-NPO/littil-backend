package org.littil.api.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import org.littil.api.exception.VerificationCodeException;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class VerificationCodeServiceTest {

    private VerificationCodeService service;

    @BeforeEach
    void setUp() {
        service = new VerificationCodeService();
    }

    @Test
    void testGetVerificationCode_Success() {
        String email = "user1@example.com";
        VerificationCode verificationCode = service.getVerificationCode(email);

        assertNotNull(verificationCode);
        assertNotNull(verificationCode.getToken());
        assertTrue(verificationCode.getToken().matches("[A-Z0-9]{3}-[A-Z0-9]{3}"));
    }

    @Test
    void testGetVerificationCode_AlreadyExists_ThrowsException() {
        String email = "user2@example.com";
        service.getVerificationCode(email);

        assertThrows(VerificationCodeException.class, () -> service.getVerificationCode(email));
    }

    @Test
    void testIsValidToken_Success() {
        String email = "user3@example.com";
        VerificationCode code = service.getVerificationCode(email);

        assertTrue(service.isValidToken(email, code.getToken()));
    }

    @Test
    void testIsValidToken_ReturnsFalse() {
        String email = "user4@example.com";
        service.getVerificationCode(email);

        assertFalse(service.isValidToken(email, "000-000"));
    }

    @Test
    void testIsValidToken_ThrowsException() {
        assertThrows(VerificationCodeException.class, () ->
                service.isValidToken("missing@example.com", "123-456"));
    }
}

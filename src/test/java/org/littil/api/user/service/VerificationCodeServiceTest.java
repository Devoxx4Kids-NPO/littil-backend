package org.littil.api.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

import java.util.NoSuchElementException;

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
        String code = service.getVerificationCode(email);

        assertNotNull(code);
        assertTrue(code.matches("\\d{3}-\\d{3}"));
    }

    @Test
    void testGetVerificationCode_AlreadyExists_ThrowsException() {
        String email = "user2@example.com";
        service.getVerificationCode(email);

        assertThrows(IllegalStateException.class, () -> service.getVerificationCode(email));
    }

    @Test
    void testIsValidVerificationCode_Success() {
        String email = "user3@example.com";
        String code = service.getVerificationCode(email);

        assertTrue(service.isValidVerificationCode(email, code));
    }

    @Test
    void testIsValidVerificationCode_InvalidCode_ReturnsFalse() {
        String email = "user4@example.com";
        service.getVerificationCode(email);

        assertFalse(service.isValidVerificationCode(email, "000-000"));
    }

    @Test
    void testIsValidVerificationCode_NoCode_ThrowsException() {
        assertThrows(NoSuchElementException.class, () ->
                service.isValidVerificationCode("missing@example.com", "123-456"));
    }

}

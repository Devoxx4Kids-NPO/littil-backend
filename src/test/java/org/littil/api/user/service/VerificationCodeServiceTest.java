package org.littil.api.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import org.littil.api.exception.VerificationCodeException;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        String token = code.getToken();

        assertTrue(service.isValidToken(userId, email, token));

        // can not validate token twice
        assertThrows(VerificationCodeException.class, () ->
                service.isValidToken(userId, email, token));
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

    @Test
    void cleanVerificationCodeMap_removesExpiredCodes() throws NoSuchFieldException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Arrange
        UUID validId = UUID.randomUUID();
        UUID expiredId = UUID.randomUUID();

        VerificationCode validCode = mock(VerificationCode.class);
        VerificationCode expiredCode = mock(VerificationCode.class);

        when(validCode.isExpired()).thenReturn(false);
        when(expiredCode.isExpired()).thenReturn(true);

        Map<UUID, VerificationCode> map = new HashMap<>();
        map.put(validId, validCode);
        map.put(expiredId, expiredCode);

        // Injecteer de map in de service (via reflection)
        var field = VerificationCodeService.class.getDeclaredField("verificationCodeMap");
        field.setAccessible(true);
        field.set(service, map);

        // Act
        var cleanMethod = VerificationCodeService.class.getDeclaredMethod("cleanVerificationCodeMap");
        cleanMethod.setAccessible(true);
        cleanMethod.invoke(service);

        // Assert
        Map<UUID, VerificationCode> result = (Map<UUID, VerificationCode>) field.get(service);
        assertEquals(1, result.size());
        assertTrue(result.containsKey(validId));
        assertFalse(result.containsKey(expiredId));
    }

}

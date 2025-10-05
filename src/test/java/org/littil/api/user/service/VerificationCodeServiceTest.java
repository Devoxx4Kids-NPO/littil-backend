package org.littil.api.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

//import java.lang.reflect.Field;
//import java.lang.reflect.Modifier;
//import java.util.HashMap;
//import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class VerificationCodeServiceTest {

    private VerificationCodeService service;

    @BeforeEach
    void setUp() throws Exception {
        service = new VerificationCodeService();
        
// TODO remove - don't use Reflection or set experationTime in application.yaml
//        // Reset static map before each test
//        Field mapField = VerificationCodeService.class.getDeclaredField("verificationCodeMap");
//        mapField.setAccessible(true);
//        mapField.set(null, new HashMap<>());
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

//    @Test
//    void testCleanVerificationCodeMap_RemovesExpiredCode_UsingReflection() throws Exception {
//        String expiredEmail = "expired@example.com";
//        String validEmail = "valid@example.com";
//
//        // Create expired and valid entries
//        VerificationCodeService.VerificationCodeDetails expiredDetails = service.new VerificationCodeDetails(expiredEmail);
//        VerificationCodeService.VerificationCodeDetails validDetails = service.new VerificationCodeDetails(validEmail);
//
//        // Use reflection to modify final expireTime of expiredDetails
//        Field expireTimeField = VerificationCodeService.VerificationCodeDetails.class.getDeclaredField("expireTime");
//        expireTimeField.setAccessible(true);
////        Field modifiersField = Field.class.getDeclaredField("modifiers");
////        modifiersField.setAccessible(true);
////        modifiersField.setInt(expireTimeField, expireTimeField.getModifiers() & ~Modifier.FINAL);
//        expireTimeField.set(expiredDetails, System.currentTimeMillis() - 1000); // expired
//
//        // Inject both entries into the map
//        Field mapField = VerificationCodeService.class.getDeclaredField("verificationCodeMap");
//        mapField.setAccessible(true);
//        Map<String, VerificationCodeService.VerificationCodeDetails> map = new HashMap<>();
//        map.put(expiredEmail, expiredDetails);
//        map.put(validEmail, validDetails);
//        mapField.set(null, map);
//
//        // Trigger cleanup
//        service.getVerificationCode("new@example.com");
//
//        // Verify expired entry was removed
//        Map<?, ?> updatedMap = (Map<?, ?>) mapField.get(null);
//        assertFalse(updatedMap.containsKey(expiredEmail));
//        assertTrue(updatedMap.containsKey(validEmail));
//    }
}

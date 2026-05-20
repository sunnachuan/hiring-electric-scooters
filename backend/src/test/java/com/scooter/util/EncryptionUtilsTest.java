package com.scooter.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionUtilsTest {

    private EncryptionUtils encryptionUtils;

    @BeforeEach
    void setUp() {
        encryptionUtils = new EncryptionUtils();
        ReflectionTestUtils.setField(encryptionUtils, "encryptionKey",
                "test-key-32-chars-long-xxxxx");
    }

    @Test
    void testEncryptAndDecrypt_RoundTrip() {
        String original = "13800138000";

        String encrypted = encryptionUtils.encrypt(original);
        String decrypted = encryptionUtils.decrypt(encrypted);

        assertEquals(original, decrypted);
        assertNotEquals(original, encrypted);
    }

    @Test
    void testEncryptAndDecrypt_SpecialCharacters() {
        String original = "test@example.com";

        String encrypted = encryptionUtils.encrypt(original);
        String decrypted = encryptionUtils.decrypt(encrypted);

        assertEquals(original, decrypted);
    }

    @Test
    void testGenerateCardNumberDisplay() {
        String display = encryptionUtils.generateCardNumberDisplay("6222021234567890");

        assertEquals("************7890", display);
    }

    @Test
    void testGenerateCardNumberDisplay_ShortNumber() {
        String display = encryptionUtils.generateCardNumberDisplay("123");

        assertEquals("****", display);
    }

    @Test
    void testIsValidCardNumber_Valid() {
        assertTrue(encryptionUtils.isValidCardNumber("4532015112830366"));
    }

    @Test
    void testIsValidCardNumber_TooShort() {
        assertFalse(encryptionUtils.isValidCardNumber("12345"));
    }
}
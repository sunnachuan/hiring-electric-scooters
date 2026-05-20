package com.scooter.util;

import com.scooter.util.PasswordPolicy.PasswordValidationResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordPolicyTest {

    private final PasswordPolicy passwordPolicy = new PasswordPolicy();

    @Test
    void testValidatePassword_Null() {
        PasswordValidationResult result = passwordPolicy.validatePassword(null);

        assertFalse(result.isValid());
        assertEquals("密码不能为空", result.getMessage());
    }

    @Test
    void testValidatePassword_Empty() {
        PasswordValidationResult result = passwordPolicy.validatePassword("");

        assertFalse(result.isValid());
        assertEquals("密码不能为空", result.getMessage());
    }

    @Test
    void testValidatePassword_TooShort() {
        PasswordValidationResult result = passwordPolicy.validatePassword("ab");

        assertFalse(result.isValid());
        assertEquals("密码长度至少需要 6 个字符", result.getMessage());
    }

    @Test
    void testValidatePassword_CommonWeakPassword() {
        PasswordValidationResult result = passwordPolicy.validatePassword("123456");

        assertFalse(result.isValid());
        assertEquals("密码必须包含至少一个小写字母", result.getMessage());
    }

    @Test
    void testValidatePassword_CommonWeakPassword_NoLowerCase() {
        PasswordValidationResult result = passwordPolicy.validatePassword("qwerty");

        assertFalse(result.isValid());
        assertEquals("密码过于简单，请使用更复杂的密码", result.getMessage());
    }

    @Test
    void testValidatePassword_CommonWeakPassword_Dictionary() {
        PasswordValidationResult result = passwordPolicy.validatePassword("password");

        assertFalse(result.isValid());
        assertEquals("密码过于简单，请使用更复杂的密码", result.getMessage());
    }

    @Test
    void testValidatePassword_Valid() {
        PasswordValidationResult result = passwordPolicy.validatePassword("MySecurePass123");

        assertTrue(result.isValid());
        assertEquals("密码符合要求", result.getMessage());
    }
}
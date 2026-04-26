package com.scooter.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
@Slf4j
public class PasswordPolicy {
    
    // 密码策略配置（临时放宽以便测试）
    private static final int MIN_LENGTH = 6;
    private static final int MAX_LENGTH = 128;
    private static final boolean REQUIRE_UPPERCASE = false;
    private static final boolean REQUIRE_LOWERCASE = true;
    private static final boolean REQUIRE_DIGIT = false;
    private static final boolean REQUIRE_SPECIAL_CHAR = false;
    private static final int MIN_SPECIAL_CHARS = 0;
    
    // 正则表达式模式
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\|,.<>/?]");
    
    /**
     * 验证密码是否符合策略要求
     */
    public PasswordValidationResult validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return new PasswordValidationResult(false, "密码不能为空");
        }
        
        // 检查长度
        if (password.length() < MIN_LENGTH) {
            return new PasswordValidationResult(false, 
                String.format("密码长度至少需要 %d 个字符", MIN_LENGTH));
        }
        
        if (password.length() > MAX_LENGTH) {
            return new PasswordValidationResult(false,
                String.format("密码长度不能超过 %d 个字符", MAX_LENGTH));
        }
        
        // 检查大写字母
        if (REQUIRE_UPPERCASE && !UPPERCASE_PATTERN.matcher(password).find()) {
            return new PasswordValidationResult(false, "密码必须包含至少一个大写字母");
        }
        
        // 检查小写字母
        if (REQUIRE_LOWERCASE && !LOWERCASE_PATTERN.matcher(password).find()) {
            return new PasswordValidationResult(false, "密码必须包含至少一个小写字母");
        }
        
        // 检查数字
        if (REQUIRE_DIGIT && !DIGIT_PATTERN.matcher(password).find()) {
            return new PasswordValidationResult(false, "密码必须包含至少一个数字");
        }
        
        // 检查特殊字符
        if (REQUIRE_SPECIAL_CHAR) {
            var specialCharMatcher = SPECIAL_CHAR_PATTERN.matcher(password);
            int specialCharCount = 0;
            while (specialCharMatcher.find()) {
                specialCharCount++;
            }
            if (specialCharCount < MIN_SPECIAL_CHARS) {
                return new PasswordValidationResult(false, 
                    String.format("密码必须包含至少 %d 个特殊字符", MIN_SPECIAL_CHARS));
            }
        }
        
        // 检查常见弱密码
        if (isCommonWeakPassword(password)) {
            return new PasswordValidationResult(false, "密码过于简单，请使用更复杂的密码");
        }
        
        return new PasswordValidationResult(true, "密码符合要求");
    }
    
    /**
     * 检查是否为常见弱密码
     */
    private boolean isCommonWeakPassword(String password) {
        String[] weakPasswords = {
            "password", "123456", "qwerty", "admin", "welcome",
            "12345678", "111111", "abc123", "password1", "123456789"
        };
        
        String lowerPassword = password.toLowerCase();
        for (String weak : weakPasswords) {
            if (lowerPassword.equals(weak) || lowerPassword.contains(weak)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 密码验证结果类
     */
    public static class PasswordValidationResult {
        private final boolean valid;
        private final String message;
        
        public PasswordValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
    }
}
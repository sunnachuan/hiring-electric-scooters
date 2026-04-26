package com.scooter.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 加密工具类 - 用于敏感数据加密
 */
@Component
public class EncryptionUtils {
    
    @Value("${encryption.secret:default-encryption-key-32-chars-long}")
    private String encryptionKey;
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    
    /**
     * 加密数据
     */
    public String encrypt(String data) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(getKeyBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("加密失败", e);
        }
    }
    
    /**
     * 解密数据
     */
    public String decrypt(String encryptedData) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(getKeyBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("解密失败", e);
        }
    }
    
    /**
     * 生成银行卡号显示格式（只显示后4位）
     */
    public String generateCardNumberDisplay(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        int length = cardNumber.length();
        return "*".repeat(length - 4) + cardNumber.substring(length - 4);
    }
    
    /**
     * 验证银行卡号格式
     */
    public boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 16 || cardNumber.length() > 19) {
            return false;
        }
        // 简单的Luhn算法验证
        return isValidLuhn(cardNumber);
    }
    
    /**
     * Luhn算法验证银行卡号
     */
    private boolean isValidLuhn(String cardNumber) {
        int sum = 0;
        boolean alternate = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cardNumber.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }
    
    private byte[] getKeyBytes() {
        // 确保密钥长度为32字节（256位）
        byte[] keyBytes = encryptionKey.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[32];
        System.arraycopy(keyBytes, 0, result, 0, Math.min(keyBytes.length, 32));
        return result;
    }
}
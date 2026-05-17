package com.scooter.util;

import com.scooter.entity.User;
import com.scooter.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * 安全工具类（增强版）
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityUtils {
    
    private final UserService userService;
    
    private static final String SECRET_KEY = "scooter_rental_key_2024"; // 生产环境应从配置读取
    private static final String ALGORITHM = "AES";
    
    /**
     * 加密敏感数据
     */
    public static String encrypt(String data) {
        try {
            SecretKeySpec secretKey = generateKey();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("数据加密失败", e);
            throw new RuntimeException("数据加密失败");
        }
    }
    
    /**
     * 解密敏感数据
     */
    public static String decrypt(String encryptedData) {
        try {
            SecretKeySpec secretKey = generateKey();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("数据解密失败", e);
            throw new RuntimeException("数据解密失败");
        }
    }
    
    /**
     * 生成密钥
     */
    private static SecretKeySpec generateKey() throws Exception {
        byte[] key = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        key = sha.digest(key);
        key = java.util.Arrays.copyOf(key, 16); // AES-128
        return new SecretKeySpec(key, ALGORITHM);
    }
    
    /**
     * 生成银行卡显示号码（只显示后4位）
     */
    public static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
    
    /**
     * 生成身份证显示号码
     */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 6) {
            return "****";
        }
        return idCard.substring(0, 6) + "****" + idCard.substring(idCard.length() - 4);
    }
    
    /**
     * 生成手机号显示号码
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return "****";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
    
    /**
     * 验证银行卡号格式
     */
    public static boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 16 || cardNumber.length() > 19) {
            return false;
        }
        return cardNumber.matches("^[0-9]{16,19}$");
    }
    
    /**
     * 验证身份证号格式
     */
    public static boolean isValidIdCard(String idCard) {
        if (idCard == null) {
            return false;
        }
        return idCard.matches("^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$");
    }
    
    /**
     * 验证手机号格式
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null) {
            return false;
        }
        return phone.matches("^1[3-9]\\d{9}$");
    }
    
    /**
     * 获取当前登录用户（优先从SecurityContext，回退到请求头）
     */
    public User getCurrentUser(HttpServletRequest request) {
        var authentication = org.springframework.security.core.context.SecurityContextHolder
            .getContext().getAuthentication();
            
        if (authentication != null && authentication.isAuthenticated()
            && !(authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
            String username = authentication.getName();
            var userOptional = userService.findByUsername(username);
            if (userOptional.isPresent()) {
                return userOptional.get();
            }
        }
        
        String userIdHeader = request.getHeader("X-User-Id");
        
        if (userIdHeader == null) {
            throw new RuntimeException("用户认证信息缺失");
        }
        
        try {
            Long userId = Long.parseLong(userIdHeader);
            
            var userOptional = userService.findByUsername(request.getHeader("X-Username"));
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                if (user.getId().equals(userId)) {
                    return user;
                }
            }
            
            var userByIdOptional = userService.findByUsername(userId.toString());
            if (userByIdOptional.isPresent()) {
                return userByIdOptional.get();
            }
            
            User user = new User();
            user.setId(userId);
            user.setUsername(request.getHeader("X-Username") != null ? request.getHeader("X-Username") : "unknown");
            user.setEmail(request.getHeader("X-Email") != null ? request.getHeader("X-Email") : "");
            user.setRole(request.getHeader("X-Role") != null ? request.getHeader("X-Role") : "USER");
            
            log.warn("用户ID {} 在数据库中未找到，使用请求头信息", userId);
            return user;
            
        } catch (NumberFormatException e) {
            throw new RuntimeException("用户ID格式错误");
        }
    }
}
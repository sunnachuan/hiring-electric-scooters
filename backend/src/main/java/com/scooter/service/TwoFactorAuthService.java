package com.scooter.service;

import com.scooter.entity.TwoFactorAuth;
import com.scooter.entity.User;
import com.scooter.repository.TwoFactorAuthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TwoFactorAuthService {
    
    private final TwoFactorAuthRepository twoFactorAuthRepository;
    
    // TOTP配置
    private static final String TOTP_ALGORITHM = "HmacSHA1";
    private static final int TIME_STEP = 30; // 30秒一个时间窗口
    private static final int CODE_LENGTH = 6; // 6位验证码
    private static final int BACKUP_CODE_COUNT = 10; // 10个备用码
    private static final int BACKUP_CODE_LENGTH = 8; // 8位备用码
    
    /**
     * 为用户启用2FA
     */
    public TwoFactorAuth enable2FA(User user) {
        String secretKey = generateSecretKey();
        List<String> backupCodes = generateBackupCodes();
        
        TwoFactorAuth twoFactorAuth = new TwoFactorAuth();
        twoFactorAuth.setUserId(user.getId());
        twoFactorAuth.setSecretKey(secretKey);
        twoFactorAuth.setBackupCodes(backupCodes.stream()
                .map(code -> "{\"code\":\"" + code + "\",\"used\":false}")
                .collect(Collectors.joining(",", "[", "]")));
        twoFactorAuth.setIsEnabled(true);
        
        return twoFactorAuthRepository.save(twoFactorAuth);
    }
    
    /**
     * 禁用用户的2FA
     */
    public void disable2FA(Long userId) {
        twoFactorAuthRepository.findByUserId(userId).ifPresent(twoFactorAuth -> {
            twoFactorAuthRepository.delete(twoFactorAuth);
            log.info("2FA已禁用 - 用户ID: {}", userId);
        });
    }
    
    /**
     * 验证TOTP验证码
     */
    public boolean verifyCode(Long userId, String code) {
        Optional<TwoFactorAuth> twoFactorAuthOpt = twoFactorAuthRepository.findByUserId(userId);
        if (twoFactorAuthOpt.isEmpty() || !twoFactorAuthOpt.get().getIsEnabled()) {
            return false;
        }
        
        TwoFactorAuth twoFactorAuth = twoFactorAuthOpt.get();
        String secretKey = twoFactorAuth.getSecretKey();
        
        // 验证备用码
        if (verifyBackupCode(twoFactorAuth, code)) {
            return true;
        }
        
        // 验证TOTP码
        long currentTime = System.currentTimeMillis() / 1000;
        long timeWindow = currentTime / TIME_STEP;
        
        // 允许前后一个时间窗口的误差
        for (int i = -1; i <= 1; i++) {
            String expectedCode = generateTOTP(secretKey, timeWindow + i);
            if (code.equals(expectedCode)) {
                twoFactorAuth.setLastVerified(LocalDateTime.now());
                twoFactorAuthRepository.save(twoFactorAuth);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 生成TOTP验证码
     */
    private String generateTOTP(String secretKey, long timeWindow) {
        try {
            byte[] key = Base64.getDecoder().decode(secretKey);
            byte[] time = new byte[8];
            
            for (int i = 7; i >= 0; i--) {
                time[i] = (byte) (timeWindow & 0xFF);
                timeWindow >>= 8;
            }
            
            SecretKeySpec signingKey = new SecretKeySpec(key, TOTP_ALGORITHM);
            Mac mac = Mac.getInstance(TOTP_ALGORITHM);
            mac.init(signingKey);
            
            byte[] hash = mac.doFinal(time);
            int offset = hash[hash.length - 1] & 0xF;
            
            long truncatedHash = 0;
            for (int i = 0; i < 4; i++) {
                truncatedHash <<= 8;
                truncatedHash |= (hash[offset + i] & 0xFF);
            }
            
            truncatedHash &= 0x7FFFFFFF;
            truncatedHash %= Math.pow(10, CODE_LENGTH);
            
            return String.format("%0" + CODE_LENGTH + "d", truncatedHash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("生成TOTP验证码失败", e);
            throw new RuntimeException("验证码生成失败");
        }
    }
    
    /**
     * 验证备用码
     */
    private boolean verifyBackupCode(TwoFactorAuth twoFactorAuth, String code) {
        String backupCodesJson = twoFactorAuth.getBackupCodes();
        if (backupCodesJson == null || backupCodesJson.isEmpty()) {
            return false;
        }
        
        try {
            // 简化处理：实际应该使用JSON解析库
            if (backupCodesJson.contains("\"" + code + "\"")) {
                // 标记备用码为已使用
                String updatedBackupCodes = backupCodesJson.replace(
                    "{\"code\":\"" + code + "\",\"used\":false}",
                    "{\"code\":\"" + code + "\",\"used\":true}"
                );
                twoFactorAuth.setBackupCodes(updatedBackupCodes);
                twoFactorAuthRepository.save(twoFactorAuth);
                return true;
            }
        } catch (Exception e) {
            log.error("验证备用码失败", e);
        }
        
        return false;
    }
    
    /**
     * 生成新的备用码
     */
    public List<String> generateNewBackupCodes(Long userId) {
        Optional<TwoFactorAuth> twoFactorAuthOpt = twoFactorAuthRepository.findByUserId(userId);
        if (twoFactorAuthOpt.isEmpty()) {
            throw new RuntimeException("用户未启用2FA");
        }
        
        TwoFactorAuth twoFactorAuth = twoFactorAuthOpt.get();
        List<String> newBackupCodes = generateBackupCodes();
        
        twoFactorAuth.setBackupCodes(newBackupCodes.stream()
                .map(code -> "{\"code\":\"" + code + "\",\"used\":false}")
                .collect(Collectors.joining(",", "[", "]")));
        
        twoFactorAuthRepository.save(twoFactorAuth);
        return newBackupCodes;
    }
    
    /**
     * 生成随机密钥
     */
    private String generateSecretKey() {
        byte[] key = new byte[20];
        new SecureRandom().nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }
    
    /**
     * 生成备用码
     */
    private List<String> generateBackupCodes() {
        SecureRandom random = new SecureRandom();
        List<String> backupCodes = new ArrayList<>();
        
        for (int i = 0; i < BACKUP_CODE_COUNT; i++) {
            StringBuilder code = new StringBuilder();
            for (int j = 0; j < BACKUP_CODE_LENGTH; j++) {
                code.append(random.nextInt(10));
            }
            backupCodes.add(code.toString());
        }
        
        return backupCodes;
    }
    
    /**
     * 获取用户的2FA状态
     */
    public Optional<TwoFactorAuth> get2FAStatus(Long userId) {
        return twoFactorAuthRepository.findByUserId(userId);
    }
}
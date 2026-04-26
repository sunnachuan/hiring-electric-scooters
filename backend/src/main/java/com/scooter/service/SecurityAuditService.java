package com.scooter.service;

import com.scooter.entity.SecurityAuditLog;
import com.scooter.repository.SecurityAuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityAuditService {
    
    private final SecurityAuditLogRepository securityAuditLogRepository;
    
    // 安全配置
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 30;
    private static final int SUSPICIOUS_IP_THRESHOLD = 10;
    
    /**
     * 记录安全事件
     */
    public void logSecurityEvent(String eventType, String eventDescription, 
                                Long userId, String username, String ipAddress, 
                                String userAgent, String deviceFingerprint, 
                                boolean success, String failureReason) {
        SecurityAuditLog auditLog = new SecurityAuditLog();
        auditLog.setEventType(eventType);
        auditLog.setEventDescription(eventDescription);
        auditLog.setUserId(userId);
        auditLog.setUsername(username);
        auditLog.setIpAddress(ipAddress);
        auditLog.setUserAgent(userAgent);
        auditLog.setDeviceFingerprint(deviceFingerprint);
        auditLog.setSuccess(success);
        auditLog.setFailureReason(failureReason);
        
        securityAuditLogRepository.save(auditLog);
        
        if (!success) {
            log.warn("安全事件失败 - 类型: {}, 用户: {}, 原因: {}", eventType, username, failureReason);
        }
    }
    
    /**
     * 检查登录尝试是否超过限制
     */
    public boolean isLoginAttemptsExceeded(String username, String ipAddress) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(LOCKOUT_DURATION_MINUTES);
        long failedAttempts = securityAuditLogRepository.countFailedLoginAttempts(username, since);
        
        return failedAttempts >= MAX_LOGIN_ATTEMPTS;
    }
    
    /**
     * 获取用户的失败登录次数
     */
    public long getFailedLoginAttempts(String username) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(LOCKOUT_DURATION_MINUTES);
        return securityAuditLogRepository.countFailedLoginAttempts(username, since);
    }
    
    /**
     * 检测可疑IP地址
     */
    public List<String> detectSuspiciousIPs() {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        List<Object[]> suspiciousIPs = securityAuditLogRepository.findSuspiciousIPs(since, SUSPICIOUS_IP_THRESHOLD);
        
        return suspiciousIPs.stream()
                .map(result -> (String) result[0])
                .toList();
    }
    
    /**
     * 获取用户的安全日志
     */
    public Page<SecurityAuditLog> getUserSecurityLogs(Long userId, Pageable pageable) {
        return securityAuditLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
    
    /**
     * 获取用户的安全日志（按用户名）
     */
    public Page<SecurityAuditLog> getUserSecurityLogs(String username, Pageable pageable) {
        return securityAuditLogRepository.findByUsernameOrderByCreatedAtDesc(username, pageable);
    }
    
    /**
     * 按事件类型获取安全日志
     */
    public Page<SecurityAuditLog> getSecurityLogsByEventType(String eventType, Pageable pageable) {
        return securityAuditLogRepository.findByEventTypeOrderByCreatedAtDesc(eventType, pageable);
    }
    
    /**
     * 按时间范围获取安全日志
     */
    public Page<SecurityAuditLog> getSecurityLogsByDateRange(LocalDateTime startDate, 
                                                            LocalDateTime endDate, 
                                                            Pageable pageable) {
        return securityAuditLogRepository.findByDateRange(startDate, endDate, pageable);
    }
    
    /**
     * 定期清理旧的安全日志（保留90天）
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void cleanupOldAuditLogs() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);
        // 这里需要实现清理逻辑，可以使用原生SQL删除旧记录
        log.info("执行安全日志清理任务");
    }
    
    /**
     * 常见的安全事件类型
     */
    public static class EventTypes {
        public static final String LOGIN_SUCCESS = "LOGIN_SUCCESS";
        public static final String LOGIN_FAILED = "LOGIN_FAILED";
        public static final String LOGIN_LOCKED = "LOGIN_LOCKED";
        public static final String PASSWORD_CHANGE = "PASSWORD_CHANGE";
        public static final String PASSWORD_RESET = "PASSWORD_RESET";
        public static final String TWO_FACTOR_ENABLED = "TWO_FACTOR_ENABLED";
        public static final String TWO_FACTOR_DISABLED = "TWO_FACTOR_DISABLED";
        public static final String TWO_FACTOR_VERIFIED = "TWO_FACTOR_VERIFIED";
        public static final String TWO_FACTOR_FAILED = "TWO_FACTOR_FAILED";
        public static final String SESSION_CREATED = "SESSION_CREATED";
        public static final String SESSION_TERMINATED = "SESSION_TERMINATED";
        public static final String SUSPICIOUS_ACTIVITY = "SUSPICIOUS_ACTIVITY";
        public static final String ACCOUNT_LOCKED = "ACCOUNT_LOCKED";
        public static final String ACCOUNT_UNLOCKED = "ACCOUNT_UNLOCKED";
    }
}
package com.scooter.service;

import com.scooter.entity.UserSession;
import com.scooter.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionManagementService {
    
    private final UserSessionRepository userSessionRepository;
    
    /**
     * 创建新会话
     */
    public UserSession createSession(Long userId, String deviceFingerprint, 
                                    String userAgent, String ipAddress, String location) {
        // 检查是否已存在相同设备的会话
        Optional<UserSession> existingSession = userSessionRepository
                .findByUserIdAndDeviceFingerprint(userId, deviceFingerprint);
        
        if (existingSession.isPresent()) {
            UserSession session = existingSession.get();
            session.setLastActivity(LocalDateTime.now());
            session.setIsActive(true);
            return userSessionRepository.save(session);
        }
        
        UserSession newSession = new UserSession();
        newSession.setUserId(userId);
        newSession.setSessionToken(generateSessionToken());
        newSession.setDeviceFingerprint(deviceFingerprint);
        newSession.setUserAgent(userAgent);
        newSession.setIpAddress(ipAddress);
        newSession.setLocation(location);
        
        return userSessionRepository.save(newSession);
    }
    
    /**
     * 验证会话
     */
    public boolean validateSession(String sessionToken) {
        Optional<UserSession> sessionOpt = userSessionRepository.findBySessionToken(sessionToken);
        if (sessionOpt.isEmpty()) {
            return false;
        }
        
        UserSession session = sessionOpt.get();
        if (!session.getIsActive() || session.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }
        
        // 更新最后活动时间
        session.setLastActivity(LocalDateTime.now());
        userSessionRepository.save(session);
        
        return true;
    }
    
    /**
     * 获取用户的所有活动会话
     */
    public List<UserSession> getUserSessions(Long userId) {
        return userSessionRepository.findByUserIdAndIsActiveTrue(userId);
    }
    
    /**
     * 终止特定会话
     */
    public boolean terminateSession(Long sessionId) {
        Optional<UserSession> sessionOpt = userSessionRepository.findById(sessionId);
        if (sessionOpt.isEmpty()) {
            return false;
        }
        
        UserSession session = sessionOpt.get();
        session.setIsActive(false);
        userSessionRepository.save(session);
        
        log.info("会话已终止 - 会话ID: {}, 用户ID: {}", sessionId, session.getUserId());
        return true;
    }
    
    /**
     * 终止用户的所有会话（除了当前会话）
     */
    public int terminateOtherSessions(Long userId, Long currentSessionId) {
        return userSessionRepository.deactivateOtherSessions(userId, currentSessionId);
    }
    
    /**
     * 终止用户的所有会话
     */
    public int terminateAllSessions(Long userId) {
        return userSessionRepository.deactivateAllSessionsByUserId(userId);
    }
    
    /**
     * 清理过期会话
     */
    @Scheduled(fixedRate = 3600000) // 每小时执行一次
    public void cleanupExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        List<UserSession> expiredSessions = userSessionRepository.findExpiredSessions(now);
        
        if (!expiredSessions.isEmpty()) {
            int deactivatedCount = userSessionRepository.deactivateExpiredSessions(now);
            log.info("清理了 {} 个过期会话", deactivatedCount);
        }
    }
    
    /**
     * 生成会话令牌
     */
    private String generateSessionToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 获取会话信息
     */
    public Optional<UserSession> getSession(String sessionToken) {
        return userSessionRepository.findBySessionToken(sessionToken);
    }
}
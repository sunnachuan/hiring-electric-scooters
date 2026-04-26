package com.scooter.repository;

import com.scooter.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    
    List<UserSession> findByUserIdAndIsActiveTrue(Long userId);
    
    Optional<UserSession> findBySessionToken(String sessionToken);
    
    Optional<UserSession> findByUserIdAndDeviceFingerprint(Long userId, String deviceFingerprint);
    
    @Query("SELECT us FROM UserSession us WHERE us.expiresAt < :now AND us.isActive = true")
    List<UserSession> findExpiredSessions(@Param("now") LocalDateTime now);
    
    @Modifying
    @Query("UPDATE UserSession us SET us.isActive = false WHERE us.expiresAt < :now")
    int deactivateExpiredSessions(@Param("now") LocalDateTime now);
    
    @Modifying
    @Query("UPDATE UserSession us SET us.isActive = false WHERE us.userId = :userId")
    int deactivateAllSessionsByUserId(@Param("userId") Long userId);
    
    @Modifying
    @Query("UPDATE UserSession us SET us.isActive = false WHERE us.userId = :userId AND us.id != :excludeSessionId")
    int deactivateOtherSessions(@Param("userId") Long userId, @Param("excludeSessionId") Long excludeSessionId);
}
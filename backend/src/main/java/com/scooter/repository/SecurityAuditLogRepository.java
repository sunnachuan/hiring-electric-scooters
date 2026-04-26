package com.scooter.repository;

import com.scooter.entity.SecurityAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SecurityAuditLogRepository extends JpaRepository<SecurityAuditLog, Long> {
    
    Page<SecurityAuditLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    Page<SecurityAuditLog> findByUsernameOrderByCreatedAtDesc(String username, Pageable pageable);
    
    Page<SecurityAuditLog> findByEventTypeOrderByCreatedAtDesc(String eventType, Pageable pageable);
    
    @Query("SELECT sal FROM SecurityAuditLog sal WHERE sal.createdAt BETWEEN :startDate AND :endDate ORDER BY sal.createdAt DESC")
    Page<SecurityAuditLog> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate, 
                                          Pageable pageable);
    
    @Query("SELECT COUNT(sal) FROM SecurityAuditLog sal WHERE sal.username = :username AND sal.eventType = 'LOGIN_FAILED' AND sal.createdAt > :since")
    long countFailedLoginAttempts(@Param("username") String username, 
                                 @Param("since") LocalDateTime since);
    
    @Query("SELECT sal.ipAddress, COUNT(sal) FROM SecurityAuditLog sal WHERE sal.eventType = 'LOGIN_FAILED' AND sal.createdAt > :since GROUP BY sal.ipAddress HAVING COUNT(sal) > :threshold")
    List<Object[]> findSuspiciousIPs(@Param("since") LocalDateTime since, 
                                    @Param("threshold") long threshold);
}
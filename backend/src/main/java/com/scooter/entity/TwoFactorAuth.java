package com.scooter.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "two_factor_auth")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorAuth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "secret_key", length = 32)
    private String secretKey;
    
    @Column(name = "backup_codes", length = 500)
    private String backupCodes; // JSON格式存储备用码
    
    @Column(name = "is_enabled")
    private Boolean isEnabled = false;
    
    @Column(name = "last_verified")
    private LocalDateTime lastVerified;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
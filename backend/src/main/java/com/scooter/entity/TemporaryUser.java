package com.scooter.entity;

import com.scooter.util.SecurityUtils;
import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 临时用户实体类（用于店员代下单）
 */
@Entity
@Table(name = "temporary_users")
@Data
public class TemporaryUser {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** 临时用户名（自动生成） */
    @Column(name = "username", nullable = false, length = 50)
    private String username;
    
    /** 真实姓名 */
    @Column(name = "real_name", nullable = false, length = 50)
    private String realName;
    
    /** 手机号 */
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;
    
    /** 身份证号（加密存储） */
    @Column(name = "id_card", length = 200)
    private String idCard;
    
    /** 身份证号显示（只显示部分） */
    @Column(name = "id_card_display", length = 20)
    private String idCardDisplay;
    
    /** 紧急联系人 */
    @Column(name = "emergency_contact", length = 50)
    private String emergencyContact;
    
    /** 紧急联系人电话 */
    @Column(name = "emergency_phone", length = 20)
    private String emergencyPhone;
    
    /** 关联的银行卡ID */
    @Column(name = "bank_card_id")
    private Long bankCardId;
    
    /** 创建店员ID */
    @Column(name = "created_by", nullable = false)
    private Long createdBy;
    
    /** 创建店员姓名 */
    @Column(name = "created_by_name", nullable = false, length = 50)
    private String createdByName;
    
    /** 用户状态：ACTIVE-活跃，INACTIVE-停用 */
    @Column(name = "status", nullable = false, length = 10)
    private String status = "ACTIVE";
    
    /** 创建时间 */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /** 最后使用时间 */
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
    
    /** 关联的银行卡 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_card_id", insertable = false, updatable = false)
    private BankCard bankCard;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        
        // 自动生成临时用户名
        if (username == null) {
            username = generateTemporaryUsername();
        }
        
        // 处理身份证号显示
        if (idCard != null && idCard.length() >= 6) {
            idCardDisplay = SecurityUtils.maskIdCard(idCard);
        }
        
        // 加密存储身份证号
        if (idCard != null && !idCard.isEmpty()) {
            this.idCard = SecurityUtils.encrypt(idCard);
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * 生成临时用户名
     */
    private String generateTemporaryUsername() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String phoneSuffix = phone.length() >= 4 ? phone.substring(phone.length() - 4) : "0000";
        return "temp_" + phoneSuffix + "_" + timestamp.substring(timestamp.length() - 6);
    }
}
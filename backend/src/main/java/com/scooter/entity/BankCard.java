package com.scooter.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 银行卡实体类
 */
@Entity
@Table(name = "bank_cards")
@Data
public class BankCard {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** 关联的用户ID */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /** 银行卡号（加密存储） */
    @Column(name = "card_number", nullable = false, length = 50)
    private String cardNumber;
    
    /** 银行卡号显示（只显示后4位） */
    @Column(name = "card_number_display", nullable = false, length = 20)
    private String cardNumberDisplay;
    
    /** 银行名称 */
    @Column(name = "bank_name", nullable = false, length = 50)
    private String bankName;
    
    /** 持卡人姓名 */
    @Column(name = "cardholder_name", nullable = false, length = 50)
    private String cardholderName;
    
    /** 卡片类型：DEBIT-借记卡，CREDIT-信用卡 */
    @Column(name = "card_type", nullable = false, length = 10)
    private String cardType;
    
    /** 有效期（格式：MM/YY） */
    @Column(name = "expiry_date", length = 10)
    private String expiryDate;
    
    /** 是否默认卡片 */
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;
    
    /** 卡片状态：ACTIVE-激活，INACTIVE-停用 */
    @Column(name = "status", nullable = false, length = 10)
    private String status = "ACTIVE";
    
    /** 创建时间 */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /** 关联的用户实体 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @JsonIgnore
    private User user;
    
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
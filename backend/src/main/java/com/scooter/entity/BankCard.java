package com.scooter.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bank_cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "card_number", nullable = false, length = 19)
    private String cardNumber;
    
    @Column(name = "card_holder_name", nullable = false)
    private String cardHolderName;
    
    @Column(name = "expiry_month", length = 2)
    private String expiryMonth;
    
    @Column(name = "expiry_year", length = 4)
    private String expiryYear;
    
    @Column(name = "cvv", length = 3)
    private String cvv;
    
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
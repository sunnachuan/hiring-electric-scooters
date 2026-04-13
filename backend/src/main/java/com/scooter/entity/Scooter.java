package com.scooter.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "scooters")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Scooter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String model;
    
    @Column(name = "image_url", length = 255)
    private String imageUrl;
    
    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity = 1;
    
    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity = 1;
    
    @Column(name = "hourly_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal hourlyRate;
    
    @Column(name = "daily_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyRate;
    
    @Column(nullable = false, length = 20)
    private String status;
    
    @Column(name = "location_id")
    private Integer locationId;
    
    @Column(name = "location_name", length = 100)
    private String locationName;
    
    @Column(name = "latitude", precision = 10, scale = 6)
    private Double latitude;
    
    @Column(name = "longitude", precision = 10, scale = 6)
    private Double longitude;
    
    @Column(name = "battery_level", precision = 5, scale = 2)
    private Double batteryLevel = 100.0; // 电量百分比，默认100%
    
    @Column(name = "total_mileage", precision = 10, scale = 2)
    private Double totalMileage = 0.0; // 总行驶里程（公里）
    
    @Column(name = "is_online")
    private Boolean isOnline = false; // 是否在线
    
    @Column(name = "last_update_time")
    private LocalDateTime lastUpdateTime; // 最后更新时间
    
    @Column(name = "qr_code", length = 100)
    private String qrCode; // 二维码标识
    
    @Column(name = "unlock_code", length = 50)
    private String unlockCode; // 解锁码
    
    @Column(name = "is_locked")
    private Boolean isLocked = true; // 是否锁定状态
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastUpdateTime = LocalDateTime.now();
        if (status == null) {
            status = "AVAILABLE";
        }
        if (batteryLevel == null) {
            batteryLevel = 100.0;
        }
        if (totalMileage == null) {
            totalMileage = 0.0;
        }
        if (isOnline == null) {
            isOnline = false;
        }
        if (isLocked == null) {
            isLocked = true;
        }
        // 生成唯一二维码和解锁码
        if (qrCode == null) {
            qrCode = "SCOOTER_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
        }
        if (unlockCode == null) {
            unlockCode = String.format("%06d", (int)(Math.random() * 1000000));
        }
    }
}
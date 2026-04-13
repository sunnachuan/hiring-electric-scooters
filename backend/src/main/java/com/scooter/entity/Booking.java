package com.scooter.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "scooter_id", nullable = false)
    private Scooter scooter;
    
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;
    
    @Column(name = "duration_type", nullable = false, length = 20)
    private String durationType;
    
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;
    
    @Column(name = "discount_applied", precision = 3, scale = 2)
    private BigDecimal discountApplied = BigDecimal.ONE;
    
    @Column(nullable = false, length = 20)
    private String status;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "distance_traveled", precision = 10, scale = 2)
    private Double distanceTraveled = 0.0; // 行驶距离（公里）
    
    @Column(name = "start_latitude", precision = 10, scale = 8)
    private Double startLatitude; // 起始位置纬度
    
    @Column(name = "start_longitude", precision = 11, scale = 8)
    private Double startLongitude; // 起始位置经度
    
    @Column(name = "end_latitude", precision = 10, scale = 8)
    private Double endLatitude; // 结束位置纬度
    
    @Column(name = "end_longitude", precision = 11, scale = 8)
    private Double endLongitude; // 结束位置经度
    
    @Column(name = "billing_type", length = 20)
    private String billingType = "TIME_ONLY"; // 计费类型：TIME_ONLY, DISTANCE_ONLY, TIME_DISTANCE
    
    @Column(name = "time_rate", precision = 10, scale = 2)
    private BigDecimal timeRate; // 时间费率
    
    @Column(name = "distance_rate", precision = 10, scale = 2)
    private BigDecimal distanceRate; // 距离费率
    
    @Column(name = "actual_end_time")
    private LocalDateTime actualEndTime; // 实际结束时间
    
    @Column(name = "overtime_minutes")
    private Integer overtimeMinutes = 0; // 超时分钟数
    
    @Column(name = "overtime_fee", precision = 10, scale = 2)
    private BigDecimal overtimeFee = BigDecimal.ZERO; // 超时费用
    
    @Column(name = "last_reminder_sent")
    private LocalDateTime lastReminderSent; // 最后提醒发送时间
    
    @Column(name = "reminder_count")
    private Integer reminderCount = 0; // 提醒次数
    
    @Column(name = "is_auto_extended")
    private Boolean isAutoExtended = false; // 是否已自动续费
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "PENDING";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
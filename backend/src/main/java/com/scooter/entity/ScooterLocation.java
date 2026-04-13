package com.scooter.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "scooter_locations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScooterLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scooter_id", nullable = false)
    private Scooter scooter;
    
    @Column(nullable = false, precision = 10, scale = 8)
    private Double latitude;
    
    @Column(nullable = false, precision = 11, scale = 8)
    private Double longitude;
    
    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;
    
    @Column(name = "speed", precision = 5, scale = 2)
    private Double speed; // 速度（km/h）
    
    @Column(name = "battery_level", precision = 5, scale = 2)
    private Double batteryLevel; // 记录时的电量
    
    @PrePersist
    protected void onCreate() {
        if (recordedAt == null) {
            recordedAt = LocalDateTime.now();
        }
    }
}
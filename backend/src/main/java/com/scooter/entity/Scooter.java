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
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = "AVAILABLE";
        }
    }
}
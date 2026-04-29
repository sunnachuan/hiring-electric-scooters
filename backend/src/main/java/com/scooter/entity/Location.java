package com.scooter.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "locations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(length = 255)
    private String address;
    
    @Column(nullable = false, precision = 10, scale = 6)
    private Double latitude;
    
    @Column(nullable = false, precision = 10, scale = 6)
    private Double longitude;
    
    @Column(nullable = false)
    private Integer capacity = 10;
    
    @Column(name = "available_count", nullable = false)
    private Integer availableCount = 0;
    
    @Column(name = "booked_count", nullable = false)
    private Integer bookedCount = 0;
    
    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
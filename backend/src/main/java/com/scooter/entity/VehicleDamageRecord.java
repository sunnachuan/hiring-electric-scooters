package com.scooter.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "vehicle_damage_records")
@Data
public class VehicleDamageRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "booking_id", nullable = false)
    private Long bookingId;
    
    @Column(name = "scooter_id", nullable = false)
    private Long scooterId;
    
    @Column(name = "reported_by_user_id", nullable = false)
    private Long reportedByUserId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "damage_level", nullable = false)
    private DamageLevel damageLevel;
    
    @Column(name = "damaged_parts", length = 1000)
    private String damagedParts; // JSON格式存储损坏部位列表
    
    @Column(name = "description", length = 2000)
    private String description;
    
    @Column(name = "image_urls", length = 2000)
    private String imageUrls; // JSON格式存储图片URL列表
    
    @Column(name = "estimated_repair_cost")
    private Double estimatedRepairCost;
    
    @Column(name = "actual_repair_cost")
    private Double actualRepairCost;
    
    @Column(name = "user_compensation")
    private Double userCompensation;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "responsibility_type")
    private ResponsibilityType responsibilityType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DamageStatus status;
    
    @Column(name = "reviewer_notes", length = 2000)
    private String reviewerNotes;
    
    @Column(name = "reviewed_by_user_id")
    private Long reviewedByUserId;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "reported_at", nullable = false)
    private LocalDateTime reportedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.reportedAt == null) {
            this.reportedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
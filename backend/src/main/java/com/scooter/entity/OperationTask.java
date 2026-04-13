package com.scooter.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "operation_tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 50)
    private String taskType; // 任务类型：CHARGING, DEPLOYMENT, COLLECTION, MAINTENANCE
    
    @Column(nullable = false, length = 20)
    private String priority = "NORMAL"; // 优先级：LOW, NORMAL, HIGH, URGENT
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scooter_id")
    private Scooter scooter;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_operator_id")
    private Operator assignedOperator;
    
    @Column(name = "target_location", length = 100)
    private String targetLocation; // 目标位置
    
    @Column(name = "target_latitude", precision = 10, scale = 8)
    private Double targetLatitude;
    
    @Column(name = "target_longitude", precision = 11, scale = 8)
    private Double targetLongitude;
    
    @Column(nullable = false, length = 20)
    private String status = "PENDING"; // 状态：PENDING, ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "estimated_duration")
    private Integer estimatedDuration; // 预计耗时（分钟）
    
    @Column(name = "actual_duration")
    private Integer actualDuration; // 实际耗时（分钟）
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "completion_notes", length = 1000)
    private String completionNotes;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
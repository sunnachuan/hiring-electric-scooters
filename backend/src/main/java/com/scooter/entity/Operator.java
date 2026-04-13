package com.scooter.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "operators")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Operator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 50)
    private String name;
    
    @Column(nullable = false, length = 20)
    private String phone;
    
    @Column(nullable = false, length = 50)
    private String email;
    
    @Column(nullable = false, length = 20)
    private String role; // 角色：CHARGER, DEPLOYER, MAINTENANCE
    
    @Column(nullable = false, length = 20)
    private String status = "ACTIVE"; // 状态：ACTIVE, INACTIVE
    
    @Column(name = "assigned_area", length = 100)
    private String assignedArea; // 负责区域
    
    @Column(name = "current_task_count")
    private Integer currentTaskCount = 0; // 当前任务数量
    
    @Column(name = "total_tasks_completed")
    private Integer totalTasksCompleted = 0; // 完成任务总数
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "last_active_time")
    private LocalDateTime lastActiveTime;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastActiveTime = LocalDateTime.now();
    }
}
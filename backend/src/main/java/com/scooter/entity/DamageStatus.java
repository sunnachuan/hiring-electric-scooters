package com.scooter.entity;

public enum DamageStatus {
    REPORTED("已报告", "用户已报告损坏，等待审核"),
    UNDER_REVIEW("审核中", "管理员正在审核损坏情况"),
    APPROVED("已确认", "损坏情况已确认，等待赔偿处理"),
    COMPENSATED("已赔偿", "用户已完成赔偿"),
    REPAIRED("已修复", "车辆已完成维修"),
    REJECTED("已拒绝", "损坏报告被拒绝"),
    CANCELLED("已取消", "损坏报告被取消");
    
    private final String displayName;
    private final String description;
    
    DamageStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}
package com.scooter.entity;

public enum ResponsibilityType {
    USER_FULL("用户全责", "用户承担全部责任", 1.0),
    USER_PARTIAL("用户部分责任", "用户承担部分责任", 0.5),
    PLATFORM_FULL("平台全责", "平台承担全部责任", 0.0),
    UNKNOWN("责任待定", "责任归属待调查", 0.0),
    NO_FAULT("无责任方", "正常磨损或不可抗力", 0.0);
    
    private final String displayName;
    private final String description;
    private final double responsibilityRate;
    
    ResponsibilityType(String displayName, String description, double responsibilityRate) {
        this.displayName = displayName;
        this.description = description;
        this.responsibilityRate = responsibilityRate;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public double getResponsibilityRate() {
        return responsibilityRate;
    }
}
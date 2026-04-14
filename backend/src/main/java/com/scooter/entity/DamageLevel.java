package com.scooter.entity;

public enum DamageLevel {
    MINOR("轻微损坏", "外观划痕、轻微磨损", 1),
    MODERATE("中等损坏", "部件松动、功能异常", 2),
    SEVERE("严重损坏", "核心部件损坏、无法使用", 3);
    
    private final String displayName;
    private final String description;
    private final int level;
    
    DamageLevel(String displayName, String description, int level) {
        this.displayName = displayName;
        this.description = description;
        this.level = level;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getLevel() {
        return level;
    }
}
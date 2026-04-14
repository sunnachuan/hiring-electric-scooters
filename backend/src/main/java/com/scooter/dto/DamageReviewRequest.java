package com.scooter.dto;

import com.scooter.entity.DamageLevel;
import com.scooter.entity.DamageStatus;
import com.scooter.entity.ResponsibilityType;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class DamageReviewRequest {
    
    @NotNull(message = "处理状态不能为空")
    private DamageStatus status;
    
    private DamageLevel confirmedDamageLevel;
    
    private ResponsibilityType responsibilityType;
    
    private Double estimatedRepairCost;
    
    private Double userCompensation;
    
    private String reviewerNotes;
}
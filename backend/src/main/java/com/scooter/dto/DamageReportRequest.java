package com.scooter.dto;

import com.scooter.entity.DamageLevel;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class DamageReportRequest {
    
    @NotNull(message = "预订ID不能为空")
    private Long bookingId;
    
    @NotNull(message = "滑板车ID不能为空")
    private Long scooterId;
    
    @NotNull(message = "损坏等级不能为空")
    private DamageLevel damageLevel;
    
    private List<String> damagedParts;
    
    private String description;
    
    private List<String> imageUrls;
}
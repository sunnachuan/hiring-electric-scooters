package com.scooter.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;

@Data
public class BookingRequest {
    @NotNull(message = "滑板车ID不能为空")
    private Long scooterId;
    
    @NotNull(message = "租赁时长不能为空")
    @Min(value = 1, message = "租赁时长不能少于1小时")
    @Max(value = 168, message = "租赁时长不能超过168小时（7天）")
    private Integer hours;
    
    // 新增：使用存储的银行卡ID（可选）
    private Long bankCardId;
    
    // 新增：直接输入卡号（可选，与bankCardId二选一）
    private String cardNumber;
}
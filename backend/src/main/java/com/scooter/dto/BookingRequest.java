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
    
    @NotNull(message = "信用卡号不能为空")
    private String cardNumber;
}
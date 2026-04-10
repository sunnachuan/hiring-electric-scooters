package com.scooter.dto;

import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;

@Data
public class AdminBookingRequest {
    @Email(message = "邮箱格式不正确")
    @NotNull(message = "用户邮箱不能为空")
    private String userEmail;
    
    @NotNull(message = "滑板车ID不能为空")
    private Long scooterId;
    
    @NotNull(message = "租赁时长不能为空")
    @Min(value = 1, message = "租赁时长不能少于1小时")
    @Max(value = 168, message = "租赁时长不能超过168小时（7天）")
    private Integer hours;
}
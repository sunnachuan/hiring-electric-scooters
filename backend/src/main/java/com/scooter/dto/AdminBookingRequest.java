package com.scooter.dto;

import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class AdminBookingRequest {
    @Email(message = "邮箱格式不正确")
    @NotNull(message = "用户邮箱不能为空")
    private String userEmail;
    
    @NotNull(message = "滑板车ID不能为空")
    private Long scooterId;
    
    @Pattern(regexp = "^(1h|4h|1d|1w)$", message = "租赁时长必须是1h、4h、1d或1w")
    private String durationType;
}
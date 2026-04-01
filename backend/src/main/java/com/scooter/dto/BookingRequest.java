package com.scooter.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class BookingRequest {
    @NotNull(message = "滑板车ID不能为空")
    private Long scooterId;
    
    @Pattern(regexp = "^(1h|4h|1d|1w)$", message = "租赁时长必须是1h、4h、1d或1w")
    private String durationType;
    
    @NotNull(message = "信用卡号不能为空")
    private String cardNumber;
}
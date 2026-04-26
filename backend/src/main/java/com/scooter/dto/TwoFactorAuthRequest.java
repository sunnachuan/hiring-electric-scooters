package com.scooter.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class TwoFactorAuthRequest {
    
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @NotBlank(message = "验证码不能为空")
    private String code;
}
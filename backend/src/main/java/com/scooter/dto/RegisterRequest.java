package com.scooter.dto;

import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class RegisterRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @Email(message = "邮箱格式不正确")
    @NotBlank(message = "邮箱不能为空")
    private String email;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    private String role = "USER";
    private Boolean isStudent = false;
    private Boolean isSenior = false;
    
    private String phone;
    private String fullName;
}
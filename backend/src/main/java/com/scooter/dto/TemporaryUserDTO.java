package com.scooter.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 临时用户数据传输对象
 */
@Data
public class TemporaryUserDTO {
    
    private Long id;
    
    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;
    
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$", 
             message = "身份证号格式不正确")
    private String idCard;
    
    @Size(max = 50, message = "紧急联系人姓名长度不能超过50个字符")
    private String emergencyContact;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "紧急联系人手机号格式不正确")
    private String emergencyPhone;
    
    /** 关联的银行卡信息 */
    private BankCardDTO bankCard;
    
    /** 创建店员ID */
    private Long createdBy;
    
    /** 创建店员姓名 */
    private String createdByName;
}
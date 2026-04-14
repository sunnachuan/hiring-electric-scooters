package com.scooter.dto;

import lombok.Data;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * 管理员代下单请求DTO
 */
@Data
public class AdminBookingRequest {
    
    /** 用户类型：EXISTING-已注册用户，NEW-新用户，GUEST-访客模式 */
    @NotBlank(message = "用户类型不能为空")
    @Pattern(regexp = "^(EXISTING|NEW|GUEST)$", message = "用户类型不正确")
    private String userType;
    
    /** 已注册用户的邮箱（仅当userType=EXISTING时必填） */
    private String userEmail;
    
    /** 滑板车ID */
    @NotNull(message = "滑板车ID不能为空")
    private Long scooterId;
    
    /** 租赁时长（小时） */
    @NotNull(message = "租赁时长不能为空")
    private Integer hours;
    
    /** 临时用户信息（仅当userType=NEW时使用） */
    @Valid
    private TemporaryUserInfo temporaryUser;
    
    /** 访客信息（仅当userType=GUEST时使用） */
    @Valid
    private GuestInfo guestInfo;
    
    /**
     * 临时用户信息
     */
    @Data
    public static class TemporaryUserInfo {
        
        @NotBlank(message = "真实姓名不能为空")
        private String realName;
        
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        private String phone;
        
        @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$", 
                 message = "身份证号格式不正确")
        private String idCard;
        
        private String emergencyContact;
        
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "紧急联系人手机号格式不正确")
        private String emergencyPhone;
        
        /** 银行卡信息 */
        @Valid
        private BankCardDTO bankCard;
    }
    
    /**
     * 访客信息
     */
    @Data
    public static class GuestInfo {
        
        @NotBlank(message = "姓名不能为空")
        private String name;
        
        @NotBlank(message = "联系方式不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        private String phone;
    }
}
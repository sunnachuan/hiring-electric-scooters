package com.scooter.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 银行卡数据传输对象
 */
@Data
public class BankCardDTO {
    
    private Long id;
    
    @NotBlank(message = "银行卡号不能为空")
    @Pattern(regexp = "^[0-9]{16,19}$", message = "银行卡号格式不正确")
    private String cardNumber;
    
    @NotBlank(message = "银行名称不能为空")
    @Size(max = 50, message = "银行名称长度不能超过50个字符")
    private String bankName;
    
    @NotBlank(message = "持卡人姓名不能为空")
    @Size(max = 50, message = "持卡人姓名长度不能超过50个字符")
    private String cardholderName;
    
    @NotBlank(message = "卡片类型不能为空")
    @Pattern(regexp = "^(DEBIT|CREDIT)$", message = "卡片类型必须是DEBIT或CREDIT")
    private String cardType;
    
    @Pattern(regexp = "^(0[1-9]|1[0-2])/[0-9]{2}$", message = "有效期格式不正确，应为MM/YY")
    private String expiryDate;
    
    private Boolean isDefault = false;
    
    private String status;
}
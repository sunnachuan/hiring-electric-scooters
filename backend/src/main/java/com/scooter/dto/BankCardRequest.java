package com.scooter.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class BankCardRequest {
    
    @NotBlank(message = "银行卡号不能为空")
    private String cardNumber;
    
    @NotBlank(message = "持卡人姓名不能为空")
    private String cardHolderName;
    
    private String expiryMonth;
    
    private String expiryYear;
    
    private String cvv;
}
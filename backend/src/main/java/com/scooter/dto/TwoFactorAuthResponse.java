package com.scooter.dto;

import lombok.Data;

@Data
public class TwoFactorAuthResponse {
    
    private boolean enabled;
    private String secretKey; // 用于生成二维码
    private String qrCodeUrl;
    private String message;
    
    public TwoFactorAuthResponse(boolean enabled, String message) {
        this.enabled = enabled;
        this.message = message;
    }
    
    public TwoFactorAuthResponse(boolean enabled, String secretKey, String qrCodeUrl, String message) {
        this.enabled = enabled;
        this.secretKey = secretKey;
        this.qrCodeUrl = qrCodeUrl;
        this.message = message;
    }
}
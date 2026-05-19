package com.scooter.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private String role;
    private String phone;
    private String fullName;
    
    public AuthResponse(String token, Long id, String username, String email, String role) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
    }
    
    public AuthResponse(String token, Long id, String username, String email, String role, String phone, String fullName) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.phone = phone;
        this.fullName = fullName;
    }
}
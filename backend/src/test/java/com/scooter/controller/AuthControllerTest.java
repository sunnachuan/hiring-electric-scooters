package com.scooter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scooter.dto.AuthRequest;
import com.scooter.dto.RegisterRequest;
import com.scooter.entity.User;
import com.scooter.service.UserService;
import com.scooter.config.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private com.scooter.service.SecurityAuditService securityAuditService;

    @MockBean
    private com.scooter.service.EmailService emailService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    void testLogin_Success() throws Exception {
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedPassword");
        testUser.setRole("USER");

        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("testuser");
        authRequest.setPassword("correctPassword");

        when(userService.findByUsername("testuser")).thenReturn(java.util.Optional.of(testUser));
        when(passwordEncoder.matches("correctPassword", "hashedPassword")).thenReturn(true);
        when(jwtUtils.generateToken("testuser")).thenReturn("test-jwt-token");
        when(securityAuditService.isLoginAttemptsExceeded(anyString(), anyString())).thenReturn(false);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-jwt-token"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void testLogin_UserNotFound() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("nonexistent");
        authRequest.setPassword("anyPassword");

        when(userService.findByUsername("nonexistent")).thenReturn(java.util.Optional.empty());
        when(securityAuditService.isLoginAttemptsExceeded(anyString(), anyString())).thenReturn(false);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegister_Success() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("StrongPass123");
        registerRequest.setRole("USER");
        registerRequest.setIsStudent(false);
        registerRequest.setIsSenior(false);

        User newUser = new User();
        newUser.setId(1L);
        newUser.setUsername("newuser");
        newUser.setEmail("newuser@example.com");
        newUser.setRole("USER");

        when(userService.createUser(anyString(), anyString(), anyString(), anyString(), 
                anyBoolean(), anyBoolean(), any(), any())).thenReturn(newUser);
        when(jwtUtils.generateToken("newuser")).thenReturn("test-jwt-token");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-jwt-token"));
    }

    @Test
    void testRegister_InvalidUsername() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("测试用户");
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("StrongPass123");
        registerRequest.setRole("USER");

        when(userService.createUser(anyString(), anyString(), anyString(), anyString(), 
                anyBoolean(), anyBoolean(), any(), any()))
                .thenThrow(new RuntimeException("用户名不能包含中文"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }
}

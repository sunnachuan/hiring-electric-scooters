package com.scooter.controller;

import com.scooter.dto.AuthRequest;
import com.scooter.dto.AuthResponse;
import com.scooter.dto.ChangePasswordRequest;
import com.scooter.dto.RegisterRequest;
import com.scooter.dto.UpdateProfileRequest;
import java.util.HashMap;
import java.util.Map;
import com.scooter.entity.User;
import com.scooter.service.EmailService;
import com.scooter.service.UserService;
import com.scooter.service.SecurityAuditService;
import com.scooter.config.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final UserService userService;
    private final SecurityAuditService securityAuditService;
    private final EmailService emailService;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody AuthRequest authRequest, 
                                             HttpServletRequest request) {
        String ipAddress = getClientIPAddress(request);
        String userAgent = request.getHeader("User-Agent");
        
        User user = userService.findByUsername(authRequest.getUsername()).orElse(null);
        
        if (user == null || !passwordEncoder.matches(authRequest.getPassword(), user.getPasswordHash())) {
            securityAuditService.logSecurityEvent(
                SecurityAuditService.EventTypes.LOGIN_FAILED,
                "登录失败：用户名或密码错误",
                user != null ? user.getId() : null,
                authRequest.getUsername(),
                ipAddress,
                userAgent,
                null,
                false,
                "用户名或密码错误"
            );
            
            if (securityAuditService.isLoginAttemptsExceeded(authRequest.getUsername(), ipAddress)) {
                securityAuditService.logSecurityEvent(
                    SecurityAuditService.EventTypes.LOGIN_LOCKED,
                    "账户因多次失败登录尝试被锁定",
                    user != null ? user.getId() : null,
                    authRequest.getUsername(),
                    ipAddress,
                    userAgent,
                    null,
                    false,
                    "登录尝试次数超过限制"
                );
                return ResponseEntity.badRequest().body("账户已被锁定，请30分钟后再试");
            }
            
            return ResponseEntity.badRequest().body("用户名或密码错误");
        }
        
        securityAuditService.logSecurityEvent(
            SecurityAuditService.EventTypes.LOGIN_SUCCESS,
            "用户登录成功",
            user.getId(),
            user.getUsername(),
            ipAddress,
            userAgent,
            null,
            true,
            null
        );
        
        String jwt = jwtUtils.generateToken(user.getUsername());
        
        return ResponseEntity.ok(new AuthResponse(jwt, user.getId(), user.getUsername(), 
                user.getEmail(), user.getRole(), user.getPhone(), user.getFullName()));
    }
    
    private String getClientIPAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        return request.getRemoteAddr();
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            User user = userService.createUser(
                    registerRequest.getUsername(),
                    registerRequest.getEmail(),
                    registerRequest.getPassword(),
                    registerRequest.getRole(),
                    registerRequest.getIsStudent(),
                    registerRequest.getIsSenior(),
                    registerRequest.getPhone(),
                    registerRequest.getFullName()
            );
            
            String jwt = jwtUtils.generateToken(user.getUsername());
            
            new Thread(() -> {
                try {
                    String registrationTime = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss"));
                    emailService.sendRegistrationSuccess(
                        registerRequest.getEmail(),
                        registerRequest.getUsername(),
                        registerRequest.getFullName(),
                        registerRequest.getPhone(),
                        registrationTime
                    );
                    log.info("注册成功邮件已发送至: {}", registerRequest.getEmail());
                } catch (Exception e) {
                    log.error("发送注册成功邮件失败: {}", e.getMessage());
                }
            }).start();
            
            log.info("用户注册成功: {}", registerRequest.getUsername());
            return ResponseEntity.ok(new AuthResponse(jwt, user.getId(), user.getUsername(), 
                    user.getEmail(), user.getRole(), user.getPhone(), user.getFullName()));
        } catch (RuntimeException e) {
            log.warn("用户注册失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("注册过程中发生未知错误: {}", e.getMessage());
            return ResponseEntity.status(500).body("注册失败，请稍后重试");
        }
    }
    
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        try {
            userService.changePassword(
                changePasswordRequest.getUsername(),
                changePasswordRequest.getCurrentPassword(),
                changePasswordRequest.getNewPassword()
            );
            return ResponseEntity.ok("密码修改成功");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest updateProfileRequest,
                                          HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body("缺少有效的认证令牌");
            }
            
            String token = authHeader.substring(7);
            
            String username = jwtUtils.extractUsername(token);
            if (!jwtUtils.validateToken(token, username)) {
                return ResponseEntity.badRequest().body("认证令牌无效或已过期");
            }
            User currentUser = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            User updatedUser = userService.updateUserProfile(
                currentUser.getId(),
                updateProfileRequest.getFullName(),
                updateProfileRequest.getEmail(),
                updateProfileRequest.getPhone()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "用户信息更新成功");
            response.put("user", updatedUser);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
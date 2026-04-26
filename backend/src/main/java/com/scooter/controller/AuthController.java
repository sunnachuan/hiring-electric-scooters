package com.scooter.controller;

import com.scooter.dto.AuthRequest;
import com.scooter.dto.AuthResponse;
import com.scooter.dto.ChangePasswordRequest;
import com.scooter.dto.RegisterRequest;
import com.scooter.dto.TwoFactorAuthRequest;
import com.scooter.dto.TwoFactorAuthResponse;
import com.scooter.entity.TwoFactorAuth;
import com.scooter.entity.User;
import com.scooter.service.EmailService;
import com.scooter.service.UserService;
import com.scooter.service.TwoFactorAuthService;
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
    private final TwoFactorAuthService twoFactorAuthService;
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
            // 记录失败的登录尝试
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
            
            // 检查是否超过登录尝试限制
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
        
        // 记录成功的登录尝试
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
        
        // 检查用户是否启用了2FA
        var twoFactorAuthOpt = twoFactorAuthService.get2FAStatus(user.getId());
        if (twoFactorAuthOpt.isPresent() && twoFactorAuthOpt.get().getIsEnabled()) {
            // 需要2FA验证，返回需要验证码的响应
            return ResponseEntity.ok(new AuthResponse(
                null, user.getId(), user.getUsername(), user.getEmail(), user.getRole(), true
            ));
        }
        
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
        
        // 发送注册成功邮件
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
            // 邮件发送失败不影响注册流程，继续返回成功响应
        }
        
        return ResponseEntity.ok(new AuthResponse(jwt, user.getId(), user.getUsername(), 
                user.getEmail(), user.getRole(), user.getPhone(), user.getFullName()));
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
    
    @PostMapping("/verify-2fa")
    public ResponseEntity<?> verifyTwoFactorAuth(@Valid @RequestBody TwoFactorAuthRequest twoFactorAuthRequest) {
        try {
            User user = userService.findByUsername(twoFactorAuthRequest.getUsername())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            boolean isValid = twoFactorAuthService.verifyCode(user.getId(), twoFactorAuthRequest.getCode());
            if (!isValid) {
                return ResponseEntity.badRequest().body("验证码无效");
            }
            
            String jwt = jwtUtils.generateToken(user.getUsername());
            return ResponseEntity.ok(new AuthResponse(jwt, user.getId(), user.getUsername(), 
                    user.getEmail(), user.getRole(), false));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/enable-2fa")
    public ResponseEntity<?> enableTwoFactorAuth(@RequestParam String username) {
        try {
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            TwoFactorAuth twoFactorAuth = twoFactorAuthService.enable2FA(user);
            
            // 生成二维码URL（简化实现）
            String qrCodeUrl = String.format("otpauth://totp/ScooterApp:%s?secret=%s&issuer=ScooterApp", 
                    user.getUsername(), twoFactorAuth.getSecretKey());
            
            return ResponseEntity.ok(new TwoFactorAuthResponse(true, 
                    twoFactorAuth.getSecretKey(), qrCodeUrl, "2FA已启用，请使用验证器应用扫描二维码"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/disable-2fa")
    public ResponseEntity<?> disableTwoFactorAuth(@RequestParam String username) {
        try {
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            twoFactorAuthService.disable2FA(user.getId());
            return ResponseEntity.ok(new TwoFactorAuthResponse(false, "2FA已禁用"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/2fa-status")
    public ResponseEntity<?> getTwoFactorAuthStatus(@RequestParam String username) {
        try {
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            var twoFactorAuthOpt = twoFactorAuthService.get2FAStatus(user.getId());
            boolean isEnabled = twoFactorAuthOpt.isPresent() && twoFactorAuthOpt.get().getIsEnabled();
            
            return ResponseEntity.ok(new TwoFactorAuthResponse(isEnabled, 
                    isEnabled ? "2FA已启用" : "2FA未启用"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
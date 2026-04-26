package com.scooter.controller;

import com.scooter.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 邮件发送测试控制器
 */
@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class EmailTestController {
    
    private final EmailService emailService;
    
    /**
     * 测试邮件发送功能
     */
    @PostMapping("/test")
    public ResponseEntity<?> sendTestEmail(@RequestBody Map<String, String> request) {
        try {
            String toEmail = request.get("toEmail");
            String subject = request.get("subject");
            String message = request.get("message");
            
            if (toEmail == null || toEmail.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("收件人邮箱不能为空");
            }
            
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss"));
            
            // 发送测试邮件
            emailService.sendTestEmail(toEmail, subject, message, currentTime);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "测试邮件发送成功");
            response.put("toEmail", toEmail);
            response.put("sentTime", currentTime);
            
            log.info("测试邮件发送成功 - 收件人: {}", toEmail);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("测试邮件发送失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "测试邮件发送失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 测试注册成功邮件模板
     */
    @PostMapping("/test-registration")
    public ResponseEntity<?> sendTestRegistrationEmail(@RequestBody Map<String, String> request) {
        try {
            String toEmail = request.get("toEmail");
            String username = request.get("username");
            String fullName = request.get("fullName");
            String phone = request.get("phone");
            
            if (toEmail == null || toEmail.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("收件人邮箱不能为空");
            }
            
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss"));
            
            // 使用注册成功邮件模板
            emailService.sendRegistrationSuccess(toEmail, username, fullName, phone, currentTime);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "注册成功测试邮件发送成功");
            response.put("toEmail", toEmail);
            response.put("username", username);
            response.put("sentTime", currentTime);
            
            log.info("注册成功测试邮件发送成功 - 收件人: {}", toEmail);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("注册成功测试邮件发送失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "注册成功测试邮件发送失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 检查邮件配置状态
     */
    @GetMapping("/status")
    public ResponseEntity<?> checkEmailStatus() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "邮件服务配置正常");
            response.put("host", "smtp.qq.com");
            response.put("port", 587);
            response.put("username", "2375738069@qq.com");
            response.put("checkTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss")));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("邮件配置检查失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "邮件配置检查失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
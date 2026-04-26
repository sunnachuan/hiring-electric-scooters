package com.scooter.controller;

import com.scooter.entity.UserSession;
import com.scooter.service.SessionManagementService;
import com.scooter.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {
    
    private final SessionManagementService sessionManagementService;
    private final UserService userService;
    
    /**
     * 获取用户的所有活动会话
     */
    @GetMapping("/user/{username}")
    public ResponseEntity<?> getUserSessions(@PathVariable String username) {
        try {
            var userOpt = userService.findByUsername(username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("用户不存在");
            }
            
            List<UserSession> sessions = sessionManagementService.getUserSessions(userOpt.get().getId());
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * 终止特定会话
     */
    @PostMapping("/terminate/{sessionId}")
    public ResponseEntity<?> terminateSession(@PathVariable Long sessionId) {
        try {
            boolean success = sessionManagementService.terminateSession(sessionId);
            if (!success) {
                return ResponseEntity.badRequest().body("会话不存在");
            }
            return ResponseEntity.ok("会话已终止");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * 终止用户的所有其他会话（除了当前会话）
     */
    @PostMapping("/terminate-other/{username}")
    public ResponseEntity<?> terminateOtherSessions(@PathVariable String username, 
                                                   @RequestParam Long currentSessionId) {
        try {
            var userOpt = userService.findByUsername(username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("用户不存在");
            }
            
            int terminatedCount = sessionManagementService.terminateOtherSessions(
                userOpt.get().getId(), currentSessionId);
            
            return ResponseEntity.ok(String.format("已终止 %d 个其他会话", terminatedCount));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * 终止用户的所有会话
     */
    @PostMapping("/terminate-all/{username}")
    public ResponseEntity<?> terminateAllSessions(@PathVariable String username) {
        try {
            var userOpt = userService.findByUsername(username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("用户不存在");
            }
            
            int terminatedCount = sessionManagementService.terminateAllSessions(userOpt.get().getId());
            
            return ResponseEntity.ok(String.format("已终止所有会话（共 %d 个）", terminatedCount));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
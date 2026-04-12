package com.scooter.controller;

import com.scooter.dto.FeedbackRequest;
import com.scooter.entity.Feedback;
import com.scooter.entity.User;
import com.scooter.service.FeedbackService;
import com.scooter.service.UserService;
import com.scooter.config.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {
    
    private final FeedbackService feedbackService;
    private final UserService userService;
    private final JwtUtils jwtUtils;
    
    @PostMapping
    public ResponseEntity<Feedback> createFeedback(@Valid @RequestBody FeedbackRequest feedbackRequest,
                                                  HttpServletRequest request) {
        // 从JWT令牌中获取当前用户
        String token = extractTokenFromRequest(request);
        if (token == null) {
            return ResponseEntity.badRequest().build();
        }
        
        String username = jwtUtils.extractUsername(token);
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Feedback feedback = feedbackService.createFeedback(user, 
                feedbackRequest.getTitle(), feedbackRequest.getDescription());
        
        return ResponseEntity.ok(feedback);
    }
    
    @GetMapping("/user")
    public ResponseEntity<List<Feedback>> getUserFeedback(HttpServletRequest request) {
        // 从JWT令牌中获取当前用户
        String token = extractTokenFromRequest(request);
        if (token == null) {
            return ResponseEntity.badRequest().build();
        }
        
        String username = jwtUtils.extractUsername(token);
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 返回当前用户的反馈（包括管理员自己的反馈）
        List<Feedback> feedbacks = feedbackService.getUserFeedback(user.getId());
        
        return ResponseEntity.ok(feedbacks);
    }
    
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
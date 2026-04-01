package com.scooter.controller;

import com.scooter.dto.FeedbackRequest;
import com.scooter.entity.Feedback;
import com.scooter.entity.User;
import com.scooter.service.FeedbackService;
import com.scooter.service.UserService;
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
    
    @PostMapping
    public ResponseEntity<Feedback> createFeedback(@Valid @RequestBody FeedbackRequest feedbackRequest,
                                                  HttpServletRequest request) {
        // 简化版本：直接使用第一个用户（开发测试用）
        User user = userService.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("没有用户存在"));
        
        Feedback feedback = feedbackService.createFeedback(user, 
                feedbackRequest.getTitle(), feedbackRequest.getDescription());
        
        return ResponseEntity.ok(feedback);
    }
    
    @GetMapping("/user")
    public ResponseEntity<List<Feedback>> getUserFeedback(HttpServletRequest request) {
        // 简化版本：返回所有反馈（开发测试用）
        List<Feedback> feedbacks = feedbackService.getAllFeedback();
        return ResponseEntity.ok(feedbacks);
    }
}
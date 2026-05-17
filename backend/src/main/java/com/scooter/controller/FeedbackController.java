package com.scooter.controller;

import com.scooter.dto.FeedbackRequest;
import com.scooter.entity.Feedback;
import com.scooter.entity.User;
import com.scooter.service.FeedbackService;
import com.scooter.util.SecurityUtils;
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
    private final SecurityUtils securityUtils;
    
    @PostMapping
    public ResponseEntity<Feedback> createFeedback(@Valid @RequestBody FeedbackRequest feedbackRequest,
                                                  HttpServletRequest request) {
        User user = securityUtils.getCurrentUser(request);
        
        Feedback feedback = feedbackService.createFeedback(user, 
                feedbackRequest.getTitle(), feedbackRequest.getDescription());
        
        return ResponseEntity.ok(feedback);
    }
    
    @GetMapping("/user")
    public ResponseEntity<List<Feedback>> getUserFeedback(HttpServletRequest request) {
        User user = securityUtils.getCurrentUser(request);
        
        List<Feedback> feedbacks = feedbackService.getUserFeedback(user.getId());
        
        return ResponseEntity.ok(feedbacks);
    }
}
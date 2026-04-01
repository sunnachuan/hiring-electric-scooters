package com.scooter.service;

import com.scooter.entity.Feedback;
import com.scooter.entity.User;
import com.scooter.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackService {
    
    private final FeedbackRepository feedbackRepository;
    
    public Feedback createFeedback(User user, String title, String description) {
        Feedback feedback = new Feedback();
        feedback.setUser(user);
        feedback.setTitle(title);
        feedback.setDescription(description);
        feedback.setPriority("LOW");
        feedback.setStatus("OPEN");
        
        return feedbackRepository.save(feedback);
    }
    
    public List<Feedback> getUserFeedback(Long userId) {
        return feedbackRepository.findByUserId(userId);
    }
    
    public List<Feedback> getAllFeedback() {
        return feedbackRepository.findAllByOrderByCreatedAtDesc();
    }
    
    public Feedback updatePriority(Long feedbackId, String priority) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("反馈不存在"));
        
        feedback.setPriority(priority);
        return feedbackRepository.save(feedback);
    }
    
    public List<Feedback> getFeedbackByPriority(String priority) {
        return feedbackRepository.findByPriorityOrderByCreatedAtDesc(priority);
    }
}
package com.scooter.controller;

import com.scooter.dto.AdminBookingRequest;
import com.scooter.entity.Booking;
import com.scooter.entity.Feedback;
import com.scooter.entity.Scooter;
import com.scooter.entity.User;
import com.scooter.service.BookingService;
import com.scooter.service.FeedbackService;
import com.scooter.service.ScooterService;
import com.scooter.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {
    
    private final BookingService bookingService;
    private final ScooterService scooterService;
    private final UserService userService;
    private final FeedbackService feedbackService;
    
    @PostMapping("/bookings")
    public ResponseEntity<Booking> createBookingForUser(@Valid @RequestBody AdminBookingRequest request) {
        User user = userService.findByEmail(request.getUserEmail())
                .orElseThrow(() -> new RuntimeException("用户不存在：" + request.getUserEmail()));
        
        Scooter scooter = scooterService.getScooterById(request.getScooterId());
        
        // 使用模拟信用卡号进行支付
        Booking booking = bookingService.createBooking(user, scooter, 
                request.getHours(), "123456789012", null);
        
        return ResponseEntity.ok(booking);
    }
    
    @GetMapping("/revenue/weekly")
    public ResponseEntity<Map<String, Object>> getWeeklyRevenue() {
        LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
        
        // 获取一周内的总收入
        BigDecimal totalRevenue = bookingService.calculateTotalRevenueSince(weekAgo);
        
        // 获取按租用时长分类的收入
        Map<String, BigDecimal> revenueByDuration = bookingService.getRevenueByDurationTypeSince(weekAgo);
        
        Map<String, Object> response = new HashMap<>();
        response.put("totalRevenue", totalRevenue);
        response.put("revenueByDuration", revenueByDuration);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/revenue/daily")
    public ResponseEntity<Map<String, Object>> getDailyRevenue() {
        LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
        
        // 获取一周内的每日收入
        Map<String, BigDecimal> revenueByDay = bookingService.getDailyRevenueSince(weekAgo);
        
        Map<String, Object> response = new HashMap<>();
        response.put("dailyRevenue", revenueByDay);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/feedback")
    public ResponseEntity<List<Feedback>> getAllFeedback() {
        return ResponseEntity.ok(feedbackService.getAllFeedback());
    }
    
    @PutMapping("/feedback/{id}/priority")
    public ResponseEntity<Feedback> updateFeedbackPriority(@PathVariable Long id,
                                                          @RequestParam String priority) {
        return ResponseEntity.ok(feedbackService.updatePriority(id, priority));
    }
    
    @PutMapping("/feedback/{id}/status")
    public ResponseEntity<Feedback> updateFeedbackStatus(@PathVariable Long id,
                                                        @RequestParam String status) {
        return ResponseEntity.ok(feedbackService.updateStatus(id, status));
    }
}
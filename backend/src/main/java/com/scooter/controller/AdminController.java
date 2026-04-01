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
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Scooter scooter = scooterService.getScooterById(request.getScooterId());
        
        // 使用模拟信用卡号进行支付
        Booking booking = bookingService.createBooking(user, scooter, 
                request.getDurationType(), "123456789012");
        
        return ResponseEntity.ok(booking);
    }
    
    @GetMapping("/revenue/weekly")
    public ResponseEntity<Map<String, Object>> getWeeklyRevenue() {
        LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
        
        // 这里需要从BookingRepository获取数据，暂时返回模拟数据
        Map<String, Object> response = new HashMap<>();
        response.put("totalRevenue", 1250.50);
        
        Map<String, Double> revenueByDuration = new HashMap<>();
        revenueByDuration.put("1h", 250.0);
        revenueByDuration.put("4h", 450.0);
        revenueByDuration.put("1d", 350.0);
        revenueByDuration.put("1w", 200.5);
        response.put("revenueByDuration", revenueByDuration);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/revenue/daily")
    public ResponseEntity<Map<String, Object>> getDailyRevenue() {
        // 返回模拟数据
        Map<String, Double> revenueByDay = new HashMap<>();
        revenueByDay.put("2024-01-01", 180.0);
        revenueByDay.put("2024-01-02", 220.5);
        revenueByDay.put("2024-01-03", 195.0);
        revenueByDay.put("2024-01-04", 210.0);
        revenueByDay.put("2024-01-05", 245.0);
        revenueByDay.put("2024-01-06", 200.0);
        revenueByDay.put("2024-01-07", 180.0);
        
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
}
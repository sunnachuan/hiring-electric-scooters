package com.scooter.controller;

import com.scooter.dto.BookingRequest;
import com.scooter.entity.Booking;
import com.scooter.entity.Scooter;
import com.scooter.entity.User;
import com.scooter.service.BookingService;
import com.scooter.service.EmailService;
import com.scooter.service.ScooterService;
import com.scooter.service.UserService;
import com.scooter.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    
    private final BookingService bookingService;
    private final ScooterService scooterService;
    private final UserService userService;
    private final EmailService emailService;
    private final SecurityUtils securityUtils;
    
    @PostMapping
    public ResponseEntity<Booking> createBooking(@Valid @RequestBody BookingRequest bookingRequest,
                                                HttpServletRequest request) {
        // 使用当前登录用户
        User user = securityUtils.getCurrentUser(request);
        
        Scooter scooter = scooterService.getScooterById(bookingRequest.getScooterId());
        
        Booking booking = bookingService.createBooking(user, scooter, 
                bookingRequest.getHours(), bookingRequest.getCardNumber(), bookingRequest.getBankCardId());
        
        // 邮件发送已经在BookingService中处理，这里不需要重复发送
        log.info("预订创建成功 - 用户: {}, 预订ID: {}", user.getUsername(), booking.getId());
        
        return ResponseEntity.ok(booking);
    }
    
    @GetMapping("/user")
    public ResponseEntity<List<Booking>> getUserBookings(HttpServletRequest request) {
        try {
            // 返回当前用户的预订
            User user = securityUtils.getCurrentUser(request);
            List<Booking> bookings = bookingService.getUserBookings(user.getId());
            return ResponseEntity.ok(bookings);
        } catch (RuntimeException e) {
            log.error("获取用户预订失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Booking> cancelBooking(@PathVariable Long id, HttpServletRequest request) {
        // 使用当前登录用户取消预订
        User user = securityUtils.getCurrentUser(request);
        
        return ResponseEntity.ok(bookingService.cancelBooking(id, user));
    }
    
    /**
     * 提前还车
     */
    @PutMapping("/{id}/return")
    public ResponseEntity<Booking> returnScooterEarly(@PathVariable Long id, HttpServletRequest request) {
        // 使用当前登录用户还车
        User user = securityUtils.getCurrentUser(request);
        
        return ResponseEntity.ok(bookingService.returnScooterEarly(id, user));
    }
    
    @PutMapping("/{id}/extend")
    public ResponseEntity<Booking> extendBooking(@PathVariable Long id,
                                                @RequestParam Integer hours,
                                                HttpServletRequest request) {
        // 使用当前登录用户
        User user = securityUtils.getCurrentUser(request);
        
        return ResponseEntity.ok(bookingService.extendBooking(id, hours, user));
    }
}
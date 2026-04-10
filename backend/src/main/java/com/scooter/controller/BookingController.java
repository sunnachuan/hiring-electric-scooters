package com.scooter.controller;

import com.scooter.dto.BookingRequest;
import com.scooter.entity.Booking;
import com.scooter.entity.Scooter;
import com.scooter.entity.User;
import com.scooter.service.BookingService;
import com.scooter.service.ScooterService;
import com.scooter.service.UserService;
import com.scooter.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {
    
    private final BookingService bookingService;
    private final ScooterService scooterService;
    private final UserService userService;
    private final SecurityUtils securityUtils;
    
    @PostMapping
    public ResponseEntity<Booking> createBooking(@Valid @RequestBody BookingRequest bookingRequest,
                                                HttpServletRequest request) {
        // 使用当前登录用户
        User user = securityUtils.getCurrentUser(request);
        
        Scooter scooter = scooterService.getScooterById(bookingRequest.getScooterId());
        
        Booking booking = bookingService.createBooking(user, scooter, 
                bookingRequest.getHours(), bookingRequest.getCardNumber());
        
        return ResponseEntity.ok(booking);
    }
    
    @GetMapping("/user")
    public ResponseEntity<List<Booking>> getUserBookings(HttpServletRequest request) {
        // 返回当前用户的预订
        User user = securityUtils.getCurrentUser(request);
        List<Booking> bookings = bookingService.getUserBookings(user.getId());
        return ResponseEntity.ok(bookings);
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
        // 简化版本：直接延长预订
        User user = userService.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("没有用户存在"));
        
        return ResponseEntity.ok(bookingService.extendBooking(id, hours, user));
    }
}
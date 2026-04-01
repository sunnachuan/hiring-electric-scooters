package com.scooter.controller;

import com.scooter.dto.BookingRequest;
import com.scooter.entity.Booking;
import com.scooter.entity.Scooter;
import com.scooter.entity.User;
import com.scooter.service.BookingService;
import com.scooter.service.ScooterService;
import com.scooter.service.UserService;
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
    
    @PostMapping
    public ResponseEntity<Booking> createBooking(@Valid @RequestBody BookingRequest bookingRequest,
                                                HttpServletRequest request) {
        // 简化版本：直接使用第一个用户（开发测试用）
        User user = userService.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("没有用户存在"));
        
        Scooter scooter = scooterService.getScooterById(bookingRequest.getScooterId());
        
        Booking booking = bookingService.createBooking(user, scooter, 
                bookingRequest.getDurationType(), bookingRequest.getCardNumber());
        
        return ResponseEntity.ok(booking);
    }
    
    @GetMapping("/user")
    public ResponseEntity<List<Booking>> getUserBookings(HttpServletRequest request) {
        // 简化版本：返回所有预订（开发测试用）
        List<Booking> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }
    
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Booking> cancelBooking(@PathVariable Long id, HttpServletRequest request) {
        // 简化版本：直接取消预订
        User user = userService.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("没有用户存在"));
        
        return ResponseEntity.ok(bookingService.cancelBooking(id, user));
    }
    
    @PutMapping("/{id}/extend")
    public ResponseEntity<Booking> extendBooking(@PathVariable Long id,
                                                @RequestParam String durationType,
                                                HttpServletRequest request) {
        // 简化版本：直接延长预订
        User user = userService.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("没有用户存在"));
        
        return ResponseEntity.ok(bookingService.extendBooking(id, durationType, user));
    }
}
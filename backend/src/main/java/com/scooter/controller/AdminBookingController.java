package com.scooter.controller;

import com.scooter.dto.AdminBookingRequest;
import com.scooter.dto.BankCardDTO;
import com.scooter.dto.TemporaryUserDTO;
import com.scooter.entity.Booking;
import com.scooter.entity.Scooter;
import com.scooter.entity.TemporaryUser;
import com.scooter.entity.User;
import com.scooter.service.BookingService;
import com.scooter.service.ScooterService;
import com.scooter.service.TemporaryUserService;
import com.scooter.service.UserService;
import com.scooter.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 管理员代下单控制器（支持混合模式）
 */
@RestController
@RequestMapping("/api/admin/mixed-bookings")
@RequiredArgsConstructor
public class AdminBookingController {
    
    private final BookingService bookingService;
    private final ScooterService scooterService;
    private final UserService userService;
    private final TemporaryUserService temporaryUserService;
    private final SecurityUtils securityUtils;
    
    /**
     * 管理员代下单（支持三种用户类型）
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createAdminBooking(
            @Valid @RequestBody AdminBookingRequest bookingRequest,
            HttpServletRequest request) {
        
        try {
            // 获取当前管理员用户
            User adminUser = securityUtils.getCurrentUser(request);
            
            // 根据用户类型处理
            User targetUser = null;
            TemporaryUser temporaryUser = null;
            
            switch (bookingRequest.getUserType()) {
                case "EXISTING":
                    // 已注册用户：通过邮箱查找
                    targetUser = findUserByEmail(bookingRequest.getUserEmail());
                    break;
                    
                case "NEW":
                    // 新用户：创建临时用户
                    temporaryUser = createTemporaryUser(bookingRequest, adminUser);
                    break;
                    
                case "GUEST":
                    // 访客模式：创建简化临时用户
                    temporaryUser = createGuestUser(bookingRequest, adminUser);
                    break;
                    
                default:
                    throw new RuntimeException("不支持的的用户类型：" + bookingRequest.getUserType());
            }
            
            // 获取滑板车
            Scooter scooter = scooterService.getScooterById(bookingRequest.getScooterId());
            
            // 创建预订
            Booking booking;
            if (targetUser != null) {
                // 已注册用户下单
                booking = bookingService.createBooking(targetUser, scooter, 
                        bookingRequest.getHours(), null, null);
            } else if (temporaryUser != null) {
                // 临时用户下单（使用临时用户ID，特殊处理）
                booking = bookingService.createTemporaryUserBooking(temporaryUser, scooter, 
                        bookingRequest.getHours());
            } else {
                throw new RuntimeException("无法确定目标用户");
            }
            
            // 更新临时用户最后使用时间
            if (temporaryUser != null) {
                temporaryUserService.updateLastUsedTime(temporaryUser.getId());
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "代下单成功");
            response.put("booking", booking);
            response.put("userType", bookingRequest.getUserType());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 通过邮箱查找用户
     */
    private User findUserByEmail(String email) {
        Optional<User> user = userService.findByEmail(email);
        if (user.isEmpty()) {
            throw new RuntimeException("用户不存在：" + email);
        }
        return user.get();
    }
    
    /**
     * 创建临时用户
     */
    private TemporaryUser createTemporaryUser(AdminBookingRequest bookingRequest, User adminUser) {
        TemporaryUserDTO temporaryUserDTO = new TemporaryUserDTO();
        temporaryUserDTO.setRealName(bookingRequest.getTemporaryUser().getRealName());
        temporaryUserDTO.setPhone(bookingRequest.getTemporaryUser().getPhone());
        temporaryUserDTO.setIdCard(bookingRequest.getTemporaryUser().getIdCard());
        temporaryUserDTO.setEmergencyContact(bookingRequest.getTemporaryUser().getEmergencyContact());
        temporaryUserDTO.setEmergencyPhone(bookingRequest.getTemporaryUser().getEmergencyPhone());
        temporaryUserDTO.setCreatedBy(adminUser.getId());
        temporaryUserDTO.setCreatedByName(adminUser.getUsername());
        
        // 设置银行卡信息
        if (bookingRequest.getTemporaryUser().getBankCard() != null) {
            temporaryUserDTO.setBankCard(bookingRequest.getTemporaryUser().getBankCard());
        }
        
        return temporaryUserService.createTemporaryUser(temporaryUserDTO, adminUser.getId());
    }
    
    /**
     * 创建访客用户
     */
    private TemporaryUser createGuestUser(AdminBookingRequest bookingRequest, User adminUser) {
        TemporaryUserDTO temporaryUserDTO = new TemporaryUserDTO();
        temporaryUserDTO.setRealName(bookingRequest.getGuestInfo().getName());
        temporaryUserDTO.setPhone(bookingRequest.getGuestInfo().getPhone());
        temporaryUserDTO.setCreatedBy(adminUser.getId());
        temporaryUserDTO.setCreatedByName(adminUser.getUsername());
        
        return temporaryUserService.createTemporaryUser(temporaryUserDTO, adminUser.getId());
    }
}
package com.scooter.controller;

import com.scooter.dto.DamageReportRequest;
import com.scooter.dto.DamageReviewRequest;
import com.scooter.entity.VehicleDamageRecord;
import com.scooter.service.VehicleDamageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/damage")
@RequiredArgsConstructor
public class VehicleDamageController {
    
    private final VehicleDamageService damageService;
    
    /**
     * 报告车辆损坏
     */
    @PostMapping("/report")
    public ResponseEntity<?> reportDamage(@Valid @RequestBody DamageReportRequest request, 
                                         Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            VehicleDamageRecord record = damageService.reportDamage(request, userId);
            return ResponseEntity.ok(record);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * 审核损坏报告（管理员）
     */
    @PutMapping("/{id}/review")
    public ResponseEntity<?> reviewDamage(@PathVariable Long id, 
                                         @Valid @RequestBody DamageReviewRequest request,
                                         Authentication authentication) {
        try {
            Long reviewerId = getUserIdFromAuthentication(authentication);
            VehicleDamageRecord record = damageService.reviewDamage(id, request, reviewerId);
            return ResponseEntity.ok(record);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * 获取用户的损坏记录
     */
    @GetMapping("/my-records")
    public ResponseEntity<List<VehicleDamageRecord>> getUserDamageRecords(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        List<VehicleDamageRecord> records = damageService.getUserDamageRecords(userId);
        return ResponseEntity.ok(records);
    }
    
    /**
     * 获取需要审核的损坏记录（管理员）
     */
    @GetMapping("/pending-review")
    public ResponseEntity<List<VehicleDamageRecord>> getPendingReviewRecords() {
        List<VehicleDamageRecord> records = damageService.getPendingReviewRecords();
        return ResponseEntity.ok(records);
    }
    
    /**
     * 获取特定预订的损坏记录
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<VehicleDamageRecord>> getDamageRecordsByBooking(@PathVariable Long bookingId) {
        List<VehicleDamageRecord> records = damageService.getDamageRecordsByBooking(bookingId);
        return ResponseEntity.ok(records);
    }
    
    /**
     * 检查预订是否已有损坏记录
     */
    @GetMapping("/booking/{bookingId}/has-damage")
    public ResponseEntity<Boolean> hasDamageRecord(@PathVariable Long bookingId) {
        boolean hasDamage = damageService.hasDamageRecord(bookingId);
        return ResponseEntity.ok(hasDamage);
    }
    
    /**
     * 获取损坏记录详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<VehicleDamageRecord> getDamageRecord(@PathVariable Long id) {
        // 这里需要实现根据ID获取损坏记录的逻辑
        // 为了简化，暂时返回空响应
        return ResponseEntity.notFound().build();
    }
    
    /**
     * 从认证信息中获取用户ID
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        // 这里需要根据实际的认证实现来获取用户ID
        // 暂时返回默认值
        return 1L;
    }
}
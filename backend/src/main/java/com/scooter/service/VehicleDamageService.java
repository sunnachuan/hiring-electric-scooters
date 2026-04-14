package com.scooter.service;

import com.scooter.dto.DamageReportRequest;
import com.scooter.dto.DamageReviewRequest;
import com.scooter.entity.*;
import com.scooter.repository.VehicleDamageRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleDamageService {
    
    private final VehicleDamageRecordRepository damageRecordRepository;
    
    /**
     * 报告车辆损坏
     */
    @Transactional
    public VehicleDamageRecord reportDamage(DamageReportRequest request, Long userId) {
        // 简化处理：直接创建损坏记录，不验证用户和预订信息
        // 检查是否已有损坏记录
        if (damageRecordRepository.existsByBookingId(request.getBookingId())) {
            throw new RuntimeException("该预订已存在损坏记录");
        }
        
        // 创建损坏记录
        VehicleDamageRecord record = new VehicleDamageRecord();
        record.setBookingId(request.getBookingId());
        record.setScooterId(request.getScooterId());
        record.setReportedByUserId(userId);
        record.setDamageLevel(request.getDamageLevel());
        
        // 处理损坏部位（转换为JSON字符串）
        if (request.getDamagedParts() != null && !request.getDamagedParts().isEmpty()) {
            String damagedPartsJson = String.join(",", request.getDamagedParts());
            record.setDamagedParts(damagedPartsJson);
        }
        
        record.setDescription(request.getDescription());
        
        // 处理图片URL（转换为JSON字符串）
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            String imageUrlsJson = String.join(",", request.getImageUrls());
            record.setImageUrls(imageUrlsJson);
        }
        
        record.setStatus(DamageStatus.REPORTED);
        record.setResponsibilityType(ResponsibilityType.UNKNOWN);
        
        // 根据损坏等级估算维修费用
        record.setEstimatedRepairCost(calculateEstimatedRepairCost(request.getDamageLevel()));
        
        VehicleDamageRecord savedRecord = damageRecordRepository.save(record);
        log.info("用户 {} 报告了滑板车 {} 的损坏，损坏等级：{}", 
                userId, request.getScooterId(), request.getDamageLevel());
        
        return savedRecord;
    }
    
    /**
     * 审核损坏报告
     */
    @Transactional
    public VehicleDamageRecord reviewDamage(Long damageId, DamageReviewRequest request, Long reviewerId) {
        VehicleDamageRecord record = damageRecordRepository.findById(damageId)
                .orElseThrow(() -> new RuntimeException("损坏记录不存在"));
        
        record.setStatus(request.getStatus());
        record.setReviewedByUserId(reviewerId);
        record.setReviewedAt(LocalDateTime.now());
        
        if (request.getConfirmedDamageLevel() != null) {
            record.setDamageLevel(request.getConfirmedDamageLevel());
        }
        
        if (request.getResponsibilityType() != null) {
            record.setResponsibilityType(request.getResponsibilityType());
        }
        
        if (request.getEstimatedRepairCost() != null) {
            record.setEstimatedRepairCost(request.getEstimatedRepairCost());
        }
        
        if (request.getUserCompensation() != null) {
            record.setUserCompensation(request.getUserCompensation());
        } else if (request.getResponsibilityType() != null && request.getEstimatedRepairCost() != null) {
            // 自动计算赔偿金额
            record.setUserCompensation(calculateUserCompensation(
                    request.getEstimatedRepairCost(), 
                    request.getResponsibilityType()
            ));
        }
        
        record.setReviewerNotes(request.getReviewerNotes());
        
        // 如果状态是已解决，设置解决时间
        if (request.getStatus() == DamageStatus.COMPENSATED || 
            request.getStatus() == DamageStatus.REPAIRED || 
            request.getStatus() == DamageStatus.REJECTED || 
            request.getStatus() == DamageStatus.CANCELLED) {
            record.setResolvedAt(LocalDateTime.now());
        }
        
        VehicleDamageRecord updatedRecord = damageRecordRepository.save(record);
        log.info("管理员 {} 审核了损坏记录 {}，状态更新为：{}", 
                reviewerId, damageId, request.getStatus());
        
        return updatedRecord;
    }
    
    /**
     * 根据损坏等级估算维修费用
     */
    private Double calculateEstimatedRepairCost(DamageLevel damageLevel) {
        switch (damageLevel) {
            case MINOR:
                return 100.0; // 轻微损坏：100元
            case MODERATE:
                return 500.0; // 中等损坏：500元
            case SEVERE:
                return 1500.0; // 严重损坏：1500元
            default:
                return 0.0;
        }
    }
    
    /**
     * 计算用户赔偿金额
     */
    private Double calculateUserCompensation(Double repairCost, ResponsibilityType responsibilityType) {
        return repairCost * responsibilityType.getResponsibilityRate();
    }
    
    /**
     * 获取用户的损坏记录
     */
    public List<VehicleDamageRecord> getUserDamageRecords(Long userId) {
        return damageRecordRepository.findByReportedByUserId(userId);
    }
    
    /**
     * 获取需要审核的损坏记录
     */
    public List<VehicleDamageRecord> getPendingReviewRecords() {
        List<DamageStatus> pendingStatuses = Arrays.asList(DamageStatus.REPORTED, DamageStatus.UNDER_REVIEW);
        return damageRecordRepository.findByStatusInOrderByReportedAtDesc(pendingStatuses);
    }
    
    /**
     * 获取特定预订的损坏记录
     */
    public List<VehicleDamageRecord> getDamageRecordsByBooking(Long bookingId) {
        return damageRecordRepository.findByBookingId(bookingId);
    }
    
    /**
     * 检查预订是否已有损坏记录
     */
    public boolean hasDamageRecord(Long bookingId) {
        return damageRecordRepository.existsByBookingId(bookingId);
    }
    
    /**
     * 获取未解决的损坏状态列表
     */
    public List<DamageStatus> getUnresolvedStatuses() {
        return Arrays.asList(DamageStatus.REPORTED, DamageStatus.UNDER_REVIEW, DamageStatus.APPROVED);
    }
}
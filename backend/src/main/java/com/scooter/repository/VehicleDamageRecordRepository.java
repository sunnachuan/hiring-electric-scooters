package com.scooter.repository;

import com.scooter.entity.VehicleDamageRecord;
import com.scooter.entity.DamageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleDamageRecordRepository extends JpaRepository<VehicleDamageRecord, Long> {
    
    // 根据预订ID查找损坏记录
    List<VehicleDamageRecord> findByBookingId(Long bookingId);
    
    // 根据滑板车ID查找损坏记录
    List<VehicleDamageRecord> findByScooterId(Long scooterId);
    
    // 根据用户ID查找损坏记录
    List<VehicleDamageRecord> findByReportedByUserId(Long userId);
    
    // 根据状态查找损坏记录
    List<VehicleDamageRecord> findByStatus(DamageStatus status);
    
    // 查找特定预订的未解决损坏记录
    List<VehicleDamageRecord> findByBookingIdAndStatusIn(Long bookingId, List<DamageStatus> statuses);
    
    // 统计特定用户的损坏记录数量
    Long countByReportedByUserId(Long userId);
    
    // 统计特定用户的未解决损坏记录数量
    Long countByReportedByUserIdAndStatusIn(Long userId, List<DamageStatus> statuses);
    
    // 查找需要审核的损坏记录
    List<VehicleDamageRecord> findByStatusInOrderByReportedAtDesc(List<DamageStatus> statuses);
    
    // 检查特定预订是否已有损坏记录
    boolean existsByBookingId(Long bookingId);
}
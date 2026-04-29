package com.scooter.repository;

import com.scooter.entity.Scooter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScooterRepository extends JpaRepository<Scooter, Long> {
    
    @Query("SELECT s FROM Scooter s WHERE s.availableQuantity > 0")
    List<Scooter> findAvailableScooters();
    
    @Query("SELECT s FROM Scooter s WHERE s.locationId = :locationId AND s.availableQuantity > 0")
    List<Scooter> findAvailableScootersByLocation(@Param("locationId") Integer locationId);
    
    @Query("SELECT s FROM Scooter s WHERE s.status = :status")
    List<Scooter> findByStatus(@Param("status") String status);
    
    // 新增查询方法
    List<Scooter> findByIsOnlineTrue();
    
    List<Scooter> findByBatteryLevelLessThanAndIsOnlineTrue(Double batteryLevel);
    
    List<Scooter> findByIsLockedFalse();
    
    @Query("SELECT s FROM Scooter s WHERE s.qrCode = :qrCode")
    Scooter findByQrCode(@Param("qrCode") String qrCode);
    
    @Query("SELECT s FROM Scooter s WHERE s.latitude IS NOT NULL AND s.longitude IS NOT NULL AND s.isOnline = true")
    List<Scooter> findOnlineScootersWithLocation();
    
    // 根据点位ID查找滑板车
    List<Scooter> findByLocationId(Integer locationId);
}
package com.scooter.service;

import com.scooter.entity.Scooter;
import com.scooter.entity.ScooterLocation;
import com.scooter.repository.BookingRepository;
import com.scooter.repository.ScooterLocationRepository;
import com.scooter.repository.ScooterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceService {
    
    private final ScooterRepository scooterRepository;
    private final ScooterLocationRepository locationRepository;
    private final BookingRepository bookingRepository;
    
    /**
     * 更新滑板车位置和状态
     */
    @Transactional
    public void updateScooterLocation(Long scooterId, Double latitude, Double longitude, 
                                     Double batteryLevel, Double speed) {
        Scooter scooter = scooterRepository.findById(scooterId)
                .orElseThrow(() -> new RuntimeException("滑板车不存在: " + scooterId));
        
        // 更新滑板车基本信息
        scooter.setLatitude(latitude);
        scooter.setLongitude(longitude);
        scooter.setBatteryLevel(batteryLevel);
        scooter.setIsOnline(true);
        scooter.setLastUpdateTime(LocalDateTime.now());
        
        // 创建位置记录
        ScooterLocation location = new ScooterLocation();
        location.setScooter(scooter);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setBatteryLevel(batteryLevel);
        location.setSpeed(speed);
        location.setRecordedAt(LocalDateTime.now());
        
        locationRepository.save(location);
        scooterRepository.save(scooter);
    }
    
    /**
     * 获取滑板车实时状态
     */
    public Scooter getScooterStatus(Long scooterId) {
        return scooterRepository.findById(scooterId)
                .orElseThrow(() -> new RuntimeException("滑板车不存在: " + scooterId));
    }
    
    /**
     * 计算滑板车行驶里程
     */
    @Transactional
    public Double calculateMileage(Long scooterId, LocalDateTime startTime, LocalDateTime endTime) {
        List<ScooterLocation> locations = locationRepository
                .findByScooterIdAndRecordedAtBetweenOrderByRecordedAtAsc(scooterId, startTime, endTime);
        
        if (locations.size() < 2) {
            return 0.0;
        }
        
        double totalDistance = 0.0;
        for (int i = 1; i < locations.size(); i++) {
            ScooterLocation prev = locations.get(i - 1);
            ScooterLocation curr = locations.get(i);
            totalDistance += calculateDistance(prev.getLatitude(), prev.getLongitude(), 
                                             curr.getLatitude(), curr.getLongitude());
        }
        
        return totalDistance;
    }
    
    /**
     * 计算两点之间的距离（使用Haversine公式）
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 地球半径（公里）
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
    
    /**
     * 标记滑板车为离线状态
     */
    @Transactional
    public void markScooterOffline(Long scooterId) {
        Scooter scooter = scooterRepository.findById(scooterId)
                .orElseThrow(() -> new RuntimeException("滑板车不存在: " + scooterId));
        
        scooter.setIsOnline(false);
        scooter.setLastUpdateTime(LocalDateTime.now());
        scooterRepository.save(scooter);
    }
    
    /**
     * 获取低电量滑板车列表
     */
    public List<Scooter> getLowBatteryScooters(Double threshold) {
        return scooterRepository.findByBatteryLevelLessThanAndIsOnlineTrue(threshold);
    }
    
    /**
     * 清理过期的位置记录
     */
    @Transactional
    public void cleanupOldLocations() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(7);
        locationRepository.deleteByRecordedAtBefore(cutoffTime);
    }
    
    /**
     * 获取所有滑板车
     */
    public List<Scooter> getAllScooters() {
        return scooterRepository.findAll();
    }
    
    /**
     * 检查滑板车是否有活跃预订
     */
    public boolean hasActiveBooking(Long scooterId) {
        // 检查是否有PENDING或ACTIVE状态的预订
        return bookingRepository.countByScooterIdAndStatusIn(scooterId, 
                List.of("PENDING", "ACTIVE")) > 0;
    }
}
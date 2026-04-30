package com.scooter.controller;

import com.scooter.entity.Scooter;
import com.scooter.service.DeviceService;
import com.scooter.service.QRCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/device")
@RequiredArgsConstructor
public class DeviceController {
    
    private final DeviceService deviceService;
    private final QRCodeService qrCodeService;
    
    /**
     * 设备上报位置和状态
     */
    @PostMapping("/update-location")
    public ResponseEntity<Map<String, Object>> updateLocation(
            @RequestParam Long scooterId,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam Double batteryLevel,
            @RequestParam(required = false) Double speed) {
        
        deviceService.updateScooterLocation(scooterId, latitude, longitude, 
                                           batteryLevel, speed != null ? speed : 0.0);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "位置更新成功");
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取滑板车实时状态
     */
    @GetMapping("/status/{scooterId}")
    public ResponseEntity<Scooter> getScooterStatus(@PathVariable Long scooterId) {
        Scooter scooter = deviceService.getScooterStatus(scooterId);
        return ResponseEntity.ok(scooter);
    }
    
    /**
     * 二维码解锁滑板车
     */
    @PostMapping("/unlock")
    public ResponseEntity<Map<String, Object>> unlockScooter(
            @RequestParam String qrCode,
            @RequestParam String unlockCode) {
        
        Scooter scooter = qrCodeService.unlockScooter(qrCode, unlockCode);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "解锁成功");
        response.put("scooter", scooter);
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 锁定滑板车
     */
    @PostMapping("/lock")
    public ResponseEntity<Map<String, Object>> lockScooter(@RequestParam String qrCode) {
        
        Scooter scooter = qrCodeService.lockScooter(qrCode);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "锁定成功");
        response.put("scooter", scooter);
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取滑板车解锁状态
     */
    @GetMapping("/status/qr/{qrCode}")
    public ResponseEntity<Scooter> getScooterStatusByQR(@PathVariable String qrCode) {
        Scooter scooter = qrCodeService.getScooterStatus(qrCode);
        return ResponseEntity.ok(scooter);
    }
    
    /**
     * 获取在线滑板车列表
     */
    @GetMapping("/online")
    public ResponseEntity<List<Scooter>> getOnlineScooters() {
        // 这里需要调用ScooterService的查询方法
        // 暂时返回空列表，后续实现
        return ResponseEntity.ok(List.of());
    }
    
    /**
     * 获取所有滑板车及其活跃预订状态
     */
    @GetMapping("/with-booking-status")
    public ResponseEntity<List<Map<String, Object>>> getScootersWithBookingStatus() {
        List<Scooter> scooters = deviceService.getAllScooters();
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Scooter scooter : scooters) {
            Map<String, Object> scooterData = new HashMap<>();
            scooterData.put("id", scooter.getId());
            scooterData.put("model", scooter.getModel());
            scooterData.put("batteryLevel", scooter.getBatteryLevel());
            scooterData.put("isOnline", scooter.getIsOnline());
            scooterData.put("isLocked", scooter.getIsLocked());
            scooterData.put("latitude", scooter.getLatitude());
            scooterData.put("longitude", scooter.getLongitude());
            scooterData.put("locationName", scooter.getLocationName());
            scooterData.put("totalMileage", scooter.getTotalMileage());
            scooterData.put("lastUpdateTime", scooter.getLastUpdateTime());
            
            // 检查是否有活跃预订
            boolean hasActiveBooking = deviceService.hasActiveBooking(scooter.getId());
            scooterData.put("hasActiveBooking", hasActiveBooking);
            
            result.add(scooterData);
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取低电量滑板车列表
     */
    @GetMapping("/low-battery")
    public ResponseEntity<List<Scooter>> getLowBatteryScooters(
            @RequestParam(defaultValue = "20.0") Double threshold) {
        
        List<Scooter> scooters = deviceService.getLowBatteryScooters(threshold);
        return ResponseEntity.ok(scooters);
    }
    
    /**
     * 计算行驶里程
     */
    @GetMapping("/mileage/{scooterId}")
    public ResponseEntity<Map<String, Object>> calculateMileage(
            @PathVariable Long scooterId,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        
        LocalDateTime start = LocalDateTime.parse(startTime);
        LocalDateTime end = LocalDateTime.parse(endTime);
        
        Double mileage = deviceService.calculateMileage(scooterId, start, end);
        
        Map<String, Object> response = new HashMap<>();
        response.put("scooterId", scooterId);
        response.put("startTime", start);
        response.put("endTime", end);
        response.put("mileage", mileage);
        response.put("unit", "km");
        
        return ResponseEntity.ok(response);
    }
}
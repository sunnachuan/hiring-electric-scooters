package com.scooter.controller;

import com.scooter.entity.Location;
import com.scooter.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {
    
    private final LocationService locationService;
    
    /**
     * 获取所有点位
     */
    @GetMapping
    public ResponseEntity<List<Location>> getAllLocations() {
        return ResponseEntity.ok(locationService.getAllLocations());
    }
    
    /**
     * 获取所有启用的点位
     */
    @GetMapping("/active")
    public ResponseEntity<List<Location>> getActiveLocations() {
        return ResponseEntity.ok(locationService.getActiveLocations());
    }
    
    /**
     * 根据ID获取点位
     */
    @GetMapping("/{id}")
    public ResponseEntity<Location> getLocationById(@PathVariable Long id) {
        return ResponseEntity.ok(locationService.getLocationById(id));
    }
    
    /**
     * 创建新点位（仅管理员）
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Location> createLocation(@RequestBody Location location) {
        return ResponseEntity.ok(locationService.createLocation(location));
    }
    
    /**
     * 更新点位信息（仅管理员）
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Location> updateLocation(@PathVariable Long id, @RequestBody Location location) {
        return ResponseEntity.ok(locationService.updateLocation(id, location));
    }
    
    /**
     * 删除点位（仅管理员）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
        locationService.deleteLocation(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 搜索点位
     */
    @GetMapping("/search")
    public ResponseEntity<List<Location>> searchLocations(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(locationService.searchLocations(keyword));
    }
    
    /**
     * 更新点位统计信息
     */
    @PutMapping("/{id}/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateLocationStatistics(@PathVariable Long id) {
        locationService.updateLocationStatistics(id);
        return ResponseEntity.ok().build();
    }
}
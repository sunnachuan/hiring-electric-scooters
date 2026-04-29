package com.scooter.service;

import com.scooter.entity.Location;
import com.scooter.entity.Scooter;
import com.scooter.repository.LocationRepository;
import com.scooter.repository.ScooterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LocationService {
    
    private final LocationRepository locationRepository;
    private final ScooterRepository scooterRepository;
    
    /**
     * 获取所有点位
     */
    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }
    
    /**
     * 获取所有启用的点位
     */
    public List<Location> getActiveLocations() {
        return locationRepository.findAllActiveLocations();
    }
    
    /**
     * 根据ID获取点位
     */
    public Location getLocationById(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("点位不存在: " + id));
    }
    
    /**
     * 创建新点位
     */
    @Transactional
    public Location createLocation(Location location) {
        // 检查名称是否已存在
        if (locationRepository.findByName(location.getName()).isPresent()) {
            throw new RuntimeException("点位名称已存在: " + location.getName());
        }
        
        return locationRepository.save(location);
    }
    
    /**
     * 更新点位信息
     */
    @Transactional
    public Location updateLocation(Long id, Location locationDetails) {
        Location location = getLocationById(id);
        
        // 检查名称是否与其他点位重复
        if (!location.getName().equals(locationDetails.getName())) {
            Optional<Location> existingLocation = locationRepository.findByName(locationDetails.getName());
            if (existingLocation.isPresent() && !existingLocation.get().getId().equals(id)) {
                throw new RuntimeException("点位名称已存在: " + locationDetails.getName());
            }
        }
        
        location.setName(locationDetails.getName());
        location.setAddress(locationDetails.getAddress());
        location.setLatitude(locationDetails.getLatitude());
        location.setLongitude(locationDetails.getLongitude());
        location.setCapacity(locationDetails.getCapacity());
        location.setStatus(locationDetails.getStatus());
        
        return locationRepository.save(location);
    }
    
    /**
     * 删除点位
     */
    @Transactional
    public void deleteLocation(Long id) {
        Location location = getLocationById(id);
        
        // 检查是否有滑板车关联到此点位
        List<Scooter> scootersAtLocation = scooterRepository.findByLocationId(id.intValue());
        if (!scootersAtLocation.isEmpty()) {
            throw new RuntimeException("无法删除点位，仍有滑板车关联到此点位");
        }
        
        locationRepository.delete(location);
    }
    
    /**
     * 搜索点位
     */
    public List<Location> searchLocations(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllLocations();
        }
        
        List<Location> byName = locationRepository.findByNameContainingIgnoreCase(keyword);
        List<Location> byAddress = locationRepository.findByAddressContainingIgnoreCase(keyword);
        
        // 合并结果并去重
        byName.addAll(byAddress.stream()
                .filter(location -> byName.stream().noneMatch(existing -> existing.getId().equals(location.getId())))
                .toList());
        
        return byName;
    }
    
    /**
     * 更新点位统计信息
     */
    @Transactional
    public void updateLocationStatistics(Long locationId) {
        Location location = getLocationById(locationId);
        
        // 获取该点位的所有滑板车
        List<Scooter> scooters = scooterRepository.findByLocationId(locationId.intValue());
        
        // 计算统计信息
        int availableCount = (int) scooters.stream()
                .filter(scooter -> "AVAILABLE".equals(scooter.getStatus()))
                .count();
        
        int bookedCount = (int) scooters.stream()
                .filter(scooter -> !"AVAILABLE".equals(scooter.getStatus()))
                .count();
        
        location.setAvailableCount(availableCount);
        location.setBookedCount(bookedCount);
        
        locationRepository.save(location);
    }
}
package com.scooter.service;

import com.scooter.entity.Scooter;
import com.scooter.repository.ScooterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScooterService {
    
    private final ScooterRepository scooterRepository;
    
    public List<Scooter> getAllScooters() {
        return scooterRepository.findAll();
    }
    
    public List<Scooter> getAvailableScooters() {
        return scooterRepository.findAvailableScooters();
    }
    
    public Scooter createScooter(String model, String imageUrl, Integer totalQuantity, Double hourlyRate, Double dailyRate, Integer locationId) {
        Scooter scooter = new Scooter();
        scooter.setModel(model);
        scooter.setImageUrl(imageUrl);
        scooter.setTotalQuantity(totalQuantity);
        scooter.setAvailableQuantity(totalQuantity);
        scooter.setHourlyRate(new BigDecimal(hourlyRate.toString()));
        scooter.setDailyRate(new BigDecimal(dailyRate.toString()));
        scooter.setStatus(totalQuantity > 0 ? "AVAILABLE" : "UNAVAILABLE");
        
        // 设置点位信息
        // 处理点位信息：如果locationId为null，则清除点位信息；如果locationId有值，则设置点位信息
        if (locationId != null) {
            scooter.setLocationId(locationId);
            scooter.setLocationName(getLocationName(locationId));
            scooter.setLatitude(getLocationLatitude(locationId));
            scooter.setLongitude(getLocationLongitude(locationId));
        } else {
            // 清除点位信息
            scooter.setLocationId(null);
            scooter.setLocationName(null);
            scooter.setLatitude(null);
            scooter.setLongitude(null);
        }
        
        return scooterRepository.save(scooter);
    }
    
    public Scooter updateScooter(Long id, String model, String imageUrl, Integer totalQuantity, Double hourlyRate, Double dailyRate, Integer locationId) {
        Scooter scooter = scooterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("滑板车不存在"));
        
        if (model != null) {
            scooter.setModel(model);
        }
        if (imageUrl != null) {
            scooter.setImageUrl(imageUrl);
        }
        if (totalQuantity != null) {
            // 更新总数量时，可用数量按比例调整
            int oldTotal = scooter.getTotalQuantity();
            int newAvailable = Math.max(0, scooter.getAvailableQuantity() + (totalQuantity - oldTotal));
            scooter.setTotalQuantity(totalQuantity);
            scooter.setAvailableQuantity(newAvailable);
            
            // 根据可用数量更新状态
            scooter.setStatus(newAvailable > 0 ? "AVAILABLE" : "UNAVAILABLE");
        }
        if (hourlyRate != null) {
            scooter.setHourlyRate(new BigDecimal(hourlyRate.toString()));
        }
        if (dailyRate != null) {
            scooter.setDailyRate(new BigDecimal(dailyRate.toString()));
        }
        if (locationId != null) {
            scooter.setLocationId(locationId);
            scooter.setLocationName(getLocationName(locationId));
            scooter.setLatitude(getLocationLatitude(locationId));
            scooter.setLongitude(getLocationLongitude(locationId));
        }
        
        return scooterRepository.save(scooter);
    }
    
    /**
     * 更新滑板车可用数量（预订时调用）
     */
    public void decrementAvailableQuantity(Long scooterId) {
        Scooter scooter = scooterRepository.findById(scooterId)
                .orElseThrow(() -> new RuntimeException("滑板车不存在"));
        
        if (scooter.getAvailableQuantity() <= 0) {
            throw new RuntimeException("该型号滑板车已全部租出");
        }
        
        scooter.setAvailableQuantity(scooter.getAvailableQuantity() - 1);
        
        // 如果可用数量为0，更新状态
        if (scooter.getAvailableQuantity() == 0) {
            scooter.setStatus("UNAVAILABLE");
        }
        
        scooterRepository.save(scooter);
    }
    
    /**
     * 恢复滑板车可用数量（取消预订时调用）
     */
    public void incrementAvailableQuantity(Long scooterId) {
        Scooter scooter = scooterRepository.findById(scooterId)
                .orElseThrow(() -> new RuntimeException("滑板车不存在"));
        
        scooter.setAvailableQuantity(scooter.getAvailableQuantity() + 1);
        
        // 如果从不可用变为可用，更新状态
        if (scooter.getAvailableQuantity() > 0 && "UNAVAILABLE".equals(scooter.getStatus())) {
            scooter.setStatus("AVAILABLE");
        }
        
        scooterRepository.save(scooter);
    }
    
    public Scooter getScooterById(Long id) {
        return scooterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("滑板车不存在"));
    }
    
    // 获取点位名称
    private String getLocationName(Integer locationId) {
        switch (locationId) {
            case 1: return "市中心广场";
            case 2: return "大学城校区";
            case 3: return "商业步行街";
            case 4: return "地铁站出口";
            case 5: return "公园入口";
            default: return "点位" + locationId;
        }
    }
    
    // 获取点位纬度
    private Double getLocationLatitude(Integer locationId) {
        switch (locationId) {
            case 1: return 39.9042;
            case 2: return 39.9896;
            case 3: return 39.9138;
            case 4: return 39.9022;
            case 5: return 39.9163;
            default: return 39.9042;
        }
    }
    
    // 获取点位经度
    private Double getLocationLongitude(Integer locationId) {
        switch (locationId) {
            case 1: return 116.4074;
            case 2: return 116.3509;
            case 3: return 116.3631;
            case 4: return 116.3912;
            case 5: return 116.3972;
            default: return 116.4074;
        }
    }
}
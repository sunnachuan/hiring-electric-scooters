package com.scooter.repository;

import com.scooter.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    
    // 根据状态查找点位
    List<Location> findByStatus(String status);
    
    // 根据名称模糊搜索
    List<Location> findByNameContainingIgnoreCase(String name);
    
    // 根据地址模糊搜索
    List<Location> findByAddressContainingIgnoreCase(String address);
    
    // 获取所有启用的点位
    @Query("SELECT l FROM Location l WHERE l.status = 'ACTIVE' ORDER BY l.id")
    List<Location> findAllActiveLocations();
    
    // 根据经纬度范围查找点位
    @Query("SELECT l FROM Location l WHERE l.latitude BETWEEN :minLat AND :maxLat AND l.longitude BETWEEN :minLng AND :maxLng")
    List<Location> findByLocationRange(@Param("minLat") Double minLat, 
                                      @Param("maxLat") Double maxLat,
                                      @Param("minLng") Double minLng, 
                                      @Param("maxLng") Double maxLng);
    
    // 检查是否存在相同名称的点位
    Optional<Location> findByName(String name);
}
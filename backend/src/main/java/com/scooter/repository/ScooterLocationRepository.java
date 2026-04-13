package com.scooter.repository;

import com.scooter.entity.ScooterLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScooterLocationRepository extends JpaRepository<ScooterLocation, Long> {
    
    // 根据滑板车ID获取最新的位置记录
    ScooterLocation findTopByScooterIdOrderByRecordedAtDesc(Long scooterId);
    
    // 获取滑板车在指定时间段内的位置记录
    List<ScooterLocation> findByScooterIdAndRecordedAtBetweenOrderByRecordedAtAsc(
            Long scooterId, LocalDateTime startTime, LocalDateTime endTime);
    
    // 计算滑板车在指定时间段内的行驶距离
    // @Query("SELECT SUM(calculate_distance(sl1.latitude, sl1.longitude, sl2.latitude, sl2.longitude)) " +
    //        "FROM ScooterLocation sl1 JOIN ScooterLocation sl2 ON sl1.id = sl2.id - 1 " +
    //        "WHERE sl1.scooter.id = :scooterId AND sl1.recordedAt BETWEEN :startTime AND :endTime")
    // Double calculateDistanceInPeriod(@Param("scooterId") Long scooterId, 
    //                                  @Param("startTime") LocalDateTime startTime, 
    //                                  @Param("endTime") LocalDateTime endTime);
    
    // 删除过期的位置记录（保留最近7天的数据）
    void deleteByRecordedAtBefore(LocalDateTime cutoffTime);
}
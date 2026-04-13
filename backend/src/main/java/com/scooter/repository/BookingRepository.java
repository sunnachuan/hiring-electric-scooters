package com.scooter.repository;

import com.scooter.entity.Booking;
import com.scooter.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT b FROM Booking b JOIN FETCH b.scooter WHERE b.user.id = :userId ORDER BY b.startTime DESC")
    List<Booking> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT b FROM Booking b JOIN FETCH b.scooter WHERE b.user.id = :userId AND b.status IN :statuses ORDER BY b.startTime DESC")
    List<Booking> findByUserIdAndStatusIn(@Param("userId") Long userId, @Param("statuses") List<String> statuses);
    
    @Query("SELECT b FROM Booking b WHERE b.user = :user AND b.startTime >= :startDate AND b.startTime <= :endDate AND b.status = 'COMPLETED'")
    List<Booking> findCompletedBookingsByUserAndDateRange(@Param("user") User user, 
                                                          @Param("startDate") LocalDateTime startDate, 
                                                          @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT b FROM Booking b WHERE b.scooter.id = :scooterId AND b.status IN ('PENDING', 'ACTIVE') AND " +
           "((b.startTime <= :endTime AND b.endTime >= :startTime))")
    List<Booking> findOverlappingBookings(@Param("scooterId") Long scooterId, 
                                         @Param("startTime") LocalDateTime startTime, 
                                         @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT SUM(b.totalPrice) FROM Booking b WHERE b.status = 'COMPLETED' AND b.startTime >= :startDate")
    Double calculateTotalRevenueSince(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT b.durationType, SUM(b.totalPrice) FROM Booking b WHERE b.status = 'COMPLETED' AND b.startTime >= :startDate GROUP BY b.durationType")
    List<Object[]> findRevenueByDurationTypeSince(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT b.startTime, SUM(b.totalPrice) FROM Booking b WHERE b.status = 'COMPLETED' AND b.startTime >= :startDate GROUP BY b.startTime")
    List<Object[]> findDailyRevenueSince(@Param("startDate") LocalDateTime startDate);
    
    // 新增查询方法 - 智能计费功能
    @Query("SELECT SUM(b.totalPrice) FROM Booking b WHERE b.status = 'COMPLETED' AND b.endTime BETWEEN :startTime AND :endTime")
    Optional<BigDecimal> calculateRevenueBetween(@Param("startTime") LocalDateTime startTime, 
                                                @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = 'COMPLETED' AND b.endTime BETWEEN :startTime AND :endTime")
    Long countCompletedBookingsBetween(@Param("startTime") LocalDateTime startTime, 
                                       @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT b.durationType, SUM(b.totalPrice) FROM Booking b WHERE b.status = 'COMPLETED' AND b.endTime BETWEEN :startTime AND :endTime GROUP BY b.durationType")
    List<Object[]> getRevenueByDurationType(@Param("startTime") LocalDateTime startTime, 
                                           @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT b FROM Booking b WHERE b.status = 'IN_PROGRESS'")
    List<Booking> findActiveBookings();
    
    @Query("SELECT b FROM Booking b WHERE b.scooter.id = :scooterId AND b.status = 'IN_PROGRESS'")
    Optional<Booking> findActiveBookingByScooterId(@Param("scooterId") Long scooterId);
    
    /**
     * 查找所有已超时的活跃预订
     */
    @Query("SELECT b FROM Booking b WHERE b.status = 'ACTIVE' AND b.endTime < :currentTime")
    List<Booking> findOvertimeBookings(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * 查找用户的所有超时预订
     */
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.status = 'ACTIVE' AND b.endTime < :currentTime")
    List<Booking> findUserOvertimeBookings(@Param("userId") Long userId, @Param("currentTime") LocalDateTime currentTime);
}
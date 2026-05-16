package com.scooter.service;

import com.scooter.entity.Booking;
import com.scooter.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BillingService {
    
    private final BookingRepository bookingRepository;
    private final DeviceService deviceService;
    
    /**
     * 计算订单总费用（基于时间和距离的复合计费）
     */
    @Transactional
    public BigDecimal calculateTotalFee(Booking booking) {
        // 获取时间费用
        BigDecimal timeFee = calculateTimeFee(booking);
        
        // 获取距离费用
        BigDecimal distanceFee = calculateDistanceFee(booking);
        
        // 根据计费类型计算总费用
        BigDecimal totalFee;
        switch (booking.getBillingType()) {
            case "TIME_ONLY":
                totalFee = timeFee;
                break;
            case "DISTANCE_ONLY":
                totalFee = distanceFee;
                break;
            case "TIME_DISTANCE":
            default:
                totalFee = timeFee.add(distanceFee);
                break;
        }
        
        // 应用折扣
        totalFee = totalFee.multiply(booking.getDiscountApplied());
        
        // 四舍五入到2位小数
        return totalFee.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * 计算时间费用
     */
    private BigDecimal calculateTimeFee(Booking booking) {
        Duration duration = Duration.between(booking.getStartTime(), booking.getEndTime());
        long minutes = duration.toMinutes();
        
        // 转换为小时（向上取整），使用 BigDecimal 避免浮点精度丢失
        BigDecimal minutesBd = BigDecimal.valueOf(minutes);
        BigDecimal sixty = new BigDecimal("60");
        BigDecimal hours = minutesBd.divide(sixty, 10, RoundingMode.CEILING);
        
        // 获取时间费率
        BigDecimal timeRate = booking.getTimeRate() != null ? 
                booking.getTimeRate() : booking.getScooter().getHourlyRate();
        
        return timeRate.multiply(hours);
    }
    
    /**
     * 计算距离费用
     */
    private BigDecimal calculateDistanceFee(Booking booking) {
        if (booking.getDistanceTraveled() == null || 
            booking.getDistanceTraveled().compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        // 获取距离费率
        BigDecimal distanceRate = booking.getDistanceRate() != null ? 
                booking.getDistanceRate() : new BigDecimal("0.50"); // 默认0.5元/公里
        
        return distanceRate.multiply(booking.getDistanceTraveled());
    }
    
    /**
     * 实时计算当前费用（用于进行中的订单）
     */
    public BigDecimal calculateCurrentFee(Booking booking) {
        if (booking.getEndTime() != null && booking.getEndTime().isBefore(LocalDateTime.now())) {
            return booking.getTotalPrice();
        }
        
        // 创建临时订单用于计算当前费用
        Booking tempBooking = new Booking();
        tempBooking.setStartTime(booking.getStartTime());
        tempBooking.setEndTime(LocalDateTime.now());
        tempBooking.setScooter(booking.getScooter());
        tempBooking.setBillingType(booking.getBillingType());
        tempBooking.setTimeRate(booking.getTimeRate());
        tempBooking.setDistanceRate(booking.getDistanceRate());
        tempBooking.setDiscountApplied(booking.getDiscountApplied());
        
        // 计算当前行驶距离
        if (booking.getStartLatitude() != null && booking.getStartLongitude() != null) {
            // 这里可以调用设备服务获取当前位置并计算距离
            // 暂时使用固定值
            tempBooking.setDistanceTraveled(calculateCurrentDistance(booking));
        }
        
        return calculateTotalFee(tempBooking);
    }
    
    /**
     * 计算当前行驶距离
     */
    private BigDecimal calculateCurrentDistance(Booking booking) {
        // 这里应该调用设备服务获取当前位置
        // 暂时返回一个估算值
        Duration duration = Duration.between(booking.getStartTime(), LocalDateTime.now());
        long minutes = duration.toMinutes();
        
        // 假设平均速度10km/h
        BigDecimal minutesBd = BigDecimal.valueOf(minutes);
        BigDecimal speed = new BigDecimal("10.00");
        BigDecimal sixty = new BigDecimal("60");
        return speed.multiply(minutesBd).divide(sixty, 2, RoundingMode.HALF_UP);
    }
    
    /**
     * 更新订单费用（当订单结束时）
     */
    @Transactional
    public Booking finalizeBookingFee(Booking booking) {
        // 计算最终费用
        BigDecimal finalFee = calculateTotalFee(booking);
        booking.setTotalPrice(finalFee);
        
        // 记录结束位置
        if (booking.getScooter().getLatitude() != null && booking.getScooter().getLongitude() != null) {
            booking.setEndLatitude(booking.getScooter().getLatitude());
            booking.setEndLongitude(booking.getScooter().getLongitude());
        }
        
        // 计算实际行驶距离
        if (booking.getStartLatitude() != null && booking.getStartLongitude() != null &&
            booking.getEndLatitude() != null && booking.getEndLongitude() != null) {
            
            double distance = calculateDistance(
                booking.getStartLatitude(), booking.getStartLongitude(),
                booking.getEndLatitude(), booking.getEndLongitude()
            );
            booking.setDistanceTraveled(BigDecimal.valueOf(distance).setScale(2, RoundingMode.HALF_UP));
        }
        
        return bookingRepository.save(booking);
    }
    
    /**
     * 计算两点之间的距离（Haversine公式）
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
     * 获取今日收入统计
     */
    public BigDecimal getTodayRevenue() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        
        return bookingRepository.calculateRevenueBetween(startOfDay, endOfDay)
                .orElse(BigDecimal.ZERO);
    }
    
    /**
     * 获取本周收入统计
     */
    public BigDecimal getWeeklyRevenue() {
        LocalDateTime startOfWeek = LocalDateTime.now().with(java.time.DayOfWeek.MONDAY)
                .withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfWeek = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        
        return bookingRepository.calculateRevenueBetween(startOfWeek, endOfWeek)
                .orElse(BigDecimal.ZERO);
    }
}
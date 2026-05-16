package com.scooter.service;

import com.scooter.entity.Booking;
import com.scooter.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OvertimeBookingService {
    
    private final BookingRepository bookingRepository;
    private final EmailService emailService;
    
    /**
     * 每5分钟检查一次超时预订
     */
    @Scheduled(fixedRate = 300000) // 5分钟
    @Transactional
    public void checkOvertimeBookings() {
        log.info("开始检查超时预订...");
        
        LocalDateTime now = LocalDateTime.now();
        
        // 查找所有已超时的活跃预订
        List<Booking> overtimeBookings = bookingRepository.findOvertimeBookings(now);
        
        for (Booking booking : overtimeBookings) {
            handleOvertimeBooking(booking, now);
        }
        
        log.info("超时预订检查完成，共处理 {} 个预订", overtimeBookings.size());
    }
    
    /**
     * 处理单个超时预订
     */
    private void handleOvertimeBooking(Booking booking, LocalDateTime now) {
        long overtimeMinutes = ChronoUnit.MINUTES.between(booking.getEndTime(), now);
        
        if (overtimeMinutes <= 0) {
            return;
        }
        
        // 更新超时信息
        booking.setOvertimeMinutes((int) overtimeMinutes);
        booking.setUpdatedAt(now);
        
        // 根据超时时间执行不同的处理逻辑
        if (overtimeMinutes <= 15) {
            handleEarlyOvertime(booking, now);
        } else if (overtimeMinutes <= 30) {
            handleModerateOvertime(booking, now);
        } else if (overtimeMinutes <= 120) {
            handleSeriousOvertime(booking, now);
        } else {
            handleCriticalOvertime(booking, now);
        }
        
        bookingRepository.save(booking);
    }
    
    /**
     * 处理超时15分钟内的预订
     */
    private void handleEarlyOvertime(Booking booking, LocalDateTime now) {
        // 检查是否需要发送提醒（每15分钟发送一次）
        if (booking.getLastReminderSent() == null || 
            ChronoUnit.MINUTES.between(booking.getLastReminderSent(), now) >= 15) {
            
            sendOvertimeReminder(booking, "您的预订已超时，请及时归还车辆");
            booking.setLastReminderSent(now);
            booking.setReminderCount(booking.getReminderCount() + 1);
        }
    }
    
    /**
     * 处理超时30分钟内的预订
     */
    private void handleModerateOvertime(Booking booking, LocalDateTime now) {
        // 发送提醒
        if (booking.getLastReminderSent() == null || 
            ChronoUnit.MINUTES.between(booking.getLastReminderSent(), now) >= 15) {
            
            sendOvertimeReminder(booking, "您的预订已超时30分钟，系统将开始自动计费");
            booking.setLastReminderSent(now);
            booking.setReminderCount(booking.getReminderCount() + 1);
        }
        
        // 开始自动计费
        if (!booking.getIsAutoExtended()) {
            startAutoExtension(booking);
        }
    }
    
    /**
     * 处理超时2小时内的预订
     */
    private void handleSeriousOvertime(Booking booking, LocalDateTime now) {
        // 发送严重提醒
        if (booking.getLastReminderSent() == null || 
            ChronoUnit.MINUTES.between(booking.getLastReminderSent(), now) >= 30) {
            
            sendOvertimeReminder(booking, "您的预订已严重超时，客服将联系您处理");
            booking.setLastReminderSent(now);
            booking.setReminderCount(booking.getReminderCount() + 1);
        }
        
        // 更新超时费用（2倍费率）
        updateOvertimeFee(booking, new BigDecimal("2.00"));
    }
    
    /**
     * 处理超时2小时以上的预订
     */
    private void handleCriticalOvertime(Booking booking, LocalDateTime now) {
        // 发送紧急提醒
        if (booking.getLastReminderSent() == null || 
            ChronoUnit.MINUTES.between(booking.getLastReminderSent(), now) >= 60) {
            
            sendOvertimeReminder(booking, "您的预订已严重超时，账户可能被暂停使用");
            booking.setLastReminderSent(now);
            booking.setReminderCount(booking.getReminderCount() + 1);
        }
        
        // 更新超时费用（3倍费率）
        updateOvertimeFee(booking, new BigDecimal("3.00"));
        
        // 超过4小时启动紧急处理
        if (ChronoUnit.HOURS.between(booking.getEndTime(), now) >= 4) {
            handleEmergencySituation(booking);
        }
    }
    
    /**
     * 开始自动续费
     */
    private void startAutoExtension(Booking booking) {
        booking.setIsAutoExtended(true);
        log.info("开始为预订 {} 自动续费", booking.getId());
    }
    
    /**
     * 更新超时费用
     */
    private void updateOvertimeFee(Booking booking, BigDecimal rateMultiplier) {
        BigDecimal baseRate = booking.getTimeRate();
        if (baseRate == null) {
            baseRate = new BigDecimal("5.00"); // 默认费率
        }
        
        // 按分钟精确计费，避免向上取整到小时导致多收费
        BigDecimal overtimeMinutes = BigDecimal.valueOf(booking.getOvertimeMinutes());
        BigDecimal sixty = new BigDecimal("60");
        BigDecimal fee = baseRate.multiply(rateMultiplier).multiply(overtimeMinutes)
                .divide(sixty, 2, RoundingMode.HALF_UP);
        
        booking.setOvertimeFee(fee);
        log.info("预订 {} 超时费用更新为: {} 元", booking.getId(), fee);
    }
    
    /**
     * 发送超时提醒
     */
    private void sendOvertimeReminder(Booking booking, String message) {
        try {
            String emailContent = String.format(
                "尊敬的 %s 用户：\n\n" +
                "%s\n\n" +
                "预订详情：\n" +
                "- 车辆型号：%s\n" +
                "- 预订结束时间：%s\n" +
                "- 当前超时：%d 分钟\n" +
                "- 超时费用：%.2f 元\n\n" +
                "请及时归还车辆以避免额外费用。\n\n" +
                "如有疑问，请联系客服。", 
                booking.getUser().getUsername(),
                message,
                booking.getScooter().getModel(),
                booking.getEndTime(),
                booking.getOvertimeMinutes(),
                booking.getOvertimeFee()
            );
            
            // 这里调用邮件服务发送提醒
            emailService.sendSimpleMessage(
                booking.getUser().getEmail(),
                "电动滑板车超时提醒",
                emailContent
            );
            
            log.info("已向用户 {} 发送超时提醒", booking.getUser().getEmail());
        } catch (Exception e) {
            log.error("发送超时提醒失败: {}", e.getMessage());
        }
    }
    
    /**
     * 处理紧急情况
     */
    private void handleEmergencySituation(Booking booking) {
        log.warn("预订 {} 已严重超时4小时以上，启动紧急处理程序", booking.getId());
        
        // 这里可以实现：
        // 1. 联系客服人员
        // 2. 启动车辆定位
        // 3. 暂停用户账户
        // 4. 记录异常事件
    }
}
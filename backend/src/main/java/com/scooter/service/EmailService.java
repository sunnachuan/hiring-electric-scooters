package com.scooter.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
@Slf4j
public class EmailService {
    
    /**
     * 发送简单邮件（模拟实现）
     */
    public void sendSimpleMessage(String to, String subject, String text) {
        // 在实际项目中，这里会调用真实的邮件服务
        // 这里只是模拟发送，记录日志
        log.info("发送邮件提醒 - 收件人: {}, 主题: {}, 内容: {}", to, subject, text);
        
        // 模拟邮件发送延迟
        try {
            Thread.sleep(100); // 100ms延迟模拟网络请求
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("邮件发送成功 - 收件人: {}", to);
    }
    
    /**
     * 发送超时提醒邮件
     */
    public void sendOvertimeReminder(String to, String username, String scooterModel, 
                                    int overtimeMinutes, BigDecimal overtimeFee) {
        String subject = "电动滑板车超时提醒";
        String text = String.format(
            "尊敬的 %s 用户：\n\n" +
            "您的电动滑板车预订已超时 %d 分钟。\n" +
            "车辆型号：%s\n" +
            "超时费用：%.2f 元\n\n" +
            "请及时归还车辆以避免额外费用。\n\n" +
            "如有疑问，请联系客服。", 
            username, overtimeMinutes, scooterModel, overtimeFee
        );
        
        sendSimpleMessage(to, subject, text);
    }
    
    /**
     * 发送预订确认邮件
     */
    public void sendBookingConfirmation(Object booking, Object user) {
        log.info("发送预订确认邮件 - 预订ID: {}, 用户: {}", 
            booking.getClass().getSimpleName(), user.getClass().getSimpleName());
    }
    
    /**
     * 发送支付确认邮件
     */
    public void sendPaymentConfirmation(Object booking, Object user) {
        log.info("发送支付确认邮件 - 预订ID: {}, 用户: {}", 
            booking.getClass().getSimpleName(), user.getClass().getSimpleName());
    }
    
    /**
     * 发送预订取消邮件
     */
    public void sendBookingCancellation(Object booking, Object user) {
        log.info("发送预订取消邮件 - 预订ID: {}, 用户: {}", 
            booking.getClass().getSimpleName(), user.getClass().getSimpleName());
    }
    
    /**
     * 发送还车确认邮件
     */
    public void sendReturnConfirmation(Object booking, Object user) {
        log.info("发送还车确认邮件 - 预订ID: {}, 用户: {}", 
            booking.getClass().getSimpleName(), user.getClass().getSimpleName());
    }
    
    /**
     * 发送预订延期邮件
     */
    public void sendBookingExtension(Object booking, Object user) {
        log.info("发送预订延期邮件 - 预订ID: {}, 用户: {}", 
            booking.getClass().getSimpleName(), user.getClass().getSimpleName());
    }
}
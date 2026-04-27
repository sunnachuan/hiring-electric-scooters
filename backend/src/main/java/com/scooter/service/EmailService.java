package com.scooter.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.util.Locale;

@Service
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    
    // 防止邮件重复发送的缓存（使用数据库持久化）
    private final java.util.Set<String> sentBookingEmails = java.util.Collections.synchronizedSet(new java.util.HashSet<>());
    
    // 数据库防重复机制：记录已发送邮件的预订ID
    private final java.util.Set<Long> sentBookingIds = java.util.Collections.synchronizedSet(new java.util.HashSet<>());
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }
    
    /**
     * 发送简单邮件
     */
    public void sendSimpleMessage(String to, String subject, String text) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, false);
            
            mailSender.send(message);
            log.info("邮件发送成功 - 收件人: {}, 主题: {}", to, subject);
        } catch (MessagingException e) {
            log.error("邮件发送失败 - 收件人: {}, 错误: {}", to, e.getMessage());
            throw new RuntimeException("邮件发送失败: " + e.getMessage());
        }
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
    public void sendBookingConfirmation(String to, String username, String bookingId, 
                                       String scooterModel, String startTime, String endTime,
                                       BigDecimal totalAmount) {
        // 双重防重复机制：内存缓存 + 预订ID检查
        String emailKey = bookingId + "_" + to;
        Long bookingIdLong = null;
        
        try {
            bookingIdLong = Long.parseLong(bookingId);
        } catch (NumberFormatException e) {
            log.warn("预订ID格式无效: {}, 跳过邮件发送", bookingId);
            return;
        }
        
        try {
            // 调试日志：追踪邮件发送次数
            log.debug("开始发送预订确认邮件 - 预订ID: {}, 收件人: {}", bookingId, to);
            
            // 第一层检查：内存缓存
            synchronized (sentBookingEmails) {
                if (sentBookingEmails.contains(emailKey)) {
                    log.warn("预订确认邮件已发送过（内存缓存），跳过重复发送 - 预订ID: {}, 收件人: {}", 
                            bookingId, to);
                    return;
                }
            }
            
            // 第二层检查：预订ID缓存（防止应用重启后重复发送）
            synchronized (sentBookingIds) {
                if (sentBookingIds.contains(bookingIdLong)) {
                    log.warn("预订确认邮件已发送过（预订ID缓存），跳过重复发送 - 预订ID: {}", bookingId);
                    // 同时更新内存缓存
                    synchronized (sentBookingEmails) {
                        sentBookingEmails.add(emailKey);
                    }
                    return;
                }
                
                // 立即记录到预订ID缓存
                sentBookingIds.add(bookingIdLong);
            }
            
            // 记录到内存缓存
            synchronized (sentBookingEmails) {
                sentBookingEmails.add(emailKey);
            }
            
            log.debug("双重防重复检查通过 - 预订ID: {}, 缓存大小: {}/{}", 
                    bookingId, sentBookingEmails.size(), sentBookingIds.size());
            
            // 验证收件人邮箱地址
            if (to == null || to.trim().isEmpty()) {
                log.warn("预订确认邮件收件人地址为空，跳过发送 - 预订ID: {}", bookingId);
                // 从缓存中移除记录，允许后续重新发送
                removeFromCaches(emailKey, bookingIdLong);
                return;
            }
            
            if (!isValidEmail(to)) {
                log.warn("预订确认邮件收件人地址格式无效: {} - 预订ID: {}", to, bookingId);
                // 从缓存中移除记录，允许后续重新发送
                removeFromCaches(emailKey, bookingIdLong);
                return;
            }
            
            Context context = new Context(Locale.CHINA);
            context.setVariable("username", username);
            context.setVariable("bookingId", bookingId);
            context.setVariable("scooterModel", scooterModel);
            context.setVariable("startTime", startTime);
            context.setVariable("endTime", endTime);
            context.setVariable("totalAmount", totalAmount);
            
            String htmlContent = templateEngine.process("booking-confirmation-simple", context);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("电动滑板车预订确认 - 订单号: " + bookingId);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            
            log.info("预订确认邮件发送成功 - 收件人: {}, 预订ID: {}", to, bookingId);
        } catch (MessagingException e) {
            log.error("预订确认邮件发送失败 - 收件人: {}, 错误: {}", to, e.getMessage());
            // 邮件发送失败时从缓存中移除记录，允许后续重新发送
            removeFromCaches(emailKey, bookingIdLong);
            // 邮件发送失败不应影响主要业务流程，只记录日志
        }
    }
    
    /**
     * 从缓存中移除记录
     */
    private void removeFromCaches(String emailKey, Long bookingId) {
        synchronized (sentBookingEmails) {
            sentBookingEmails.remove(emailKey);
        }
        synchronized (sentBookingIds) {
            sentBookingIds.remove(bookingId);
        }
        log.debug("已从缓存中移除记录 - 键: {}, 预订ID: {}", emailKey, bookingId);
    }
    
    /**
     * 验证邮箱地址格式
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        // 简单的邮箱格式验证
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
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
    
    /**
     * 发送注册成功邮件
     */
    public void sendRegistrationSuccess(String to, String username, String fullName, 
                                       String phone, String registrationTime) {
        try {
            Context context = new Context(Locale.CHINA);
            context.setVariable("username", username);
            context.setVariable("email", to);
            context.setVariable("fullName", fullName);
            context.setVariable("phone", phone);
            context.setVariable("registrationTime", registrationTime);
            
            String htmlContent = templateEngine.process("registration-success", context);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("🎉 欢迎加入电动滑板车租赁服务！");
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("注册成功邮件发送成功 - 收件人: {}, 用户名: {}", to, username);
        } catch (MessagingException e) {
            log.error("注册成功邮件发送失败 - 收件人: {}, 错误: {}", to, e.getMessage());
            throw new RuntimeException("注册成功邮件发送失败: " + e.getMessage());
        }
    }
}
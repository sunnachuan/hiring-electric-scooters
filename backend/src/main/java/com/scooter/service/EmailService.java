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
        try {
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
            throw new RuntimeException("预订确认邮件发送失败: " + e.getMessage());
        }
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
    
    /**
     * 发送测试邮件
     */
    public void sendTestEmail(String to, String subject, String message, String sentTime) {
        try {
            Context context = new Context(Locale.CHINA);
            context.setVariable("toEmail", to);
            context.setVariable("subject", subject);
            context.setVariable("message", message);
            context.setVariable("sentTime", sentTime);
            context.setVariable("fromEmail", fromEmail);
            
            String htmlContent = templateEngine.process("test-email", context);
            
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("测试邮件 - " + subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(mimeMessage);
            log.info("测试邮件发送成功 - 收件人: {}, 主题: {}", to, subject);
        } catch (MessagingException e) {
            log.error("测试邮件发送失败 - 收件人: {}, 错误: {}", to, e.getMessage());
            throw new RuntimeException("测试邮件发送失败: " + e.getMessage());
        }
    }
}
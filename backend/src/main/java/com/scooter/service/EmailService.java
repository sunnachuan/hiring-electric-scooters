package com.scooter.service;

import com.scooter.entity.Booking;
import com.scooter.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    
    /**
     * 发送预订确认邮件
     */
    public void sendBookingConfirmation(Booking booking, User user) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("user", user);
            variables.put("booking", booking);
            variables.put("scooter", booking.getScooter());
            
            String subject = "电动滑板车预订确认 - 订单 #" + booking.getId();
            String content = buildEmailContent("booking-confirmation", variables);
            
            sendEmail(user.getEmail(), subject, content);
            log.info("预订确认邮件已发送至: {}", user.getEmail());
        } catch (Exception e) {
            log.error("发送预订确认邮件失败: {}", e.getMessage());
        }
    }
    
    /**
     * 发送支付成功邮件
     */
    public void sendPaymentConfirmation(Booking booking, User user) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("user", user);
            variables.put("booking", booking);
            variables.put("scooter", booking.getScooter());
            
            String subject = "支付成功确认 - 订单 #" + booking.getId();
            String content = buildEmailContent("payment-confirmation", variables);
            
            sendEmail(user.getEmail(), subject, content);
            log.info("支付确认邮件已发送至: {}", user.getEmail());
        } catch (Exception e) {
            log.error("发送支付确认邮件失败: {}", e.getMessage());
        }
    }
    
    /**
     * 发送预订取消邮件
     */
    public void sendBookingCancellation(Booking booking, User user) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("user", user);
            variables.put("booking", booking);
            variables.put("scooter", booking.getScooter());
            
            String subject = "预订取消确认 - 订单 #" + booking.getId();
            String content = buildEmailContent("booking-cancellation", variables);
            
            sendEmail(user.getEmail(), subject, content);
            log.info("预订取消邮件已发送至: {}", user.getEmail());
        } catch (Exception e) {
            log.error("发送预订取消邮件失败: {}", e.getMessage());
        }
    }
    
    /**
     * 发送预订延长邮件
     */
    public void sendBookingExtension(Booking booking, User user) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("user", user);
            variables.put("booking", booking);
            variables.put("scooter", booking.getScooter());
            
            String subject = "预订延长确认 - 订单 #" + booking.getId();
            String content = buildEmailContent("booking-extension", variables);
            
            sendEmail(user.getEmail(), subject, content);
            log.info("预订延长邮件已发送至: {}", user.getEmail());
        } catch (Exception e) {
            log.error("发送预订延长邮件失败: {}", e.getMessage());
        }
    }
    
    /**
     * 构建邮件内容
     */
    private String buildEmailContent(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }
    
    /**
     * 发送邮件
     */
    private void sendEmail(String to, String subject, String content) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true); // true表示HTML内容
        
        mailSender.send(message);
    }
}
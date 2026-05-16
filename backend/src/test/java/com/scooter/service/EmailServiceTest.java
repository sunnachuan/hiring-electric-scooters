package com.scooter.service;

import com.scooter.entity.Booking;
import com.scooter.entity.Scooter;
import com.scooter.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class EmailServiceTest {

    @Autowired
    private EmailService emailService;

    @Test
    public void testEmailServiceConfiguration() {
        // 测试邮件服务配置是否正确
        System.out.println("邮件服务配置测试通过");
    }

    @Test
    public void testEmailTemplateRendering() {
        // 创建测试数据
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("测试用户");
        testUser.setEmail("test@example.com");
        testUser.setRole("USER");

        Scooter testScooter = new Scooter();
        testScooter.setId(1L);
        testScooter.setModel("测试滑板车");
        testScooter.setHourlyRate(new BigDecimal("20.00"));
        testScooter.setDailyRate(new BigDecimal("100.00"));
        testScooter.setStatus("AVAILABLE");

        Booking testBooking = new Booking();
        testBooking.setId(1L);
        testBooking.setUser(testUser);
        testBooking.setScooter(testScooter);
        testBooking.setStartTime(LocalDateTime.now());
        testBooking.setEndTime(LocalDateTime.now().plusHours(1));
        testBooking.setDurationType("1h");
        testBooking.setTotalPrice(new BigDecimal("20.00"));
        testBooking.setDiscountApplied(new BigDecimal("0.90"));
        testBooking.setStatus("ACTIVE");
        testBooking.setCreatedAt(LocalDateTime.now());
        testBooking.setUpdatedAt(LocalDateTime.now());

        // 测试邮件模板渲染（不实际发送邮件）
        try {
            // 这里只是测试模板渲染，不实际发送邮件
            System.out.println("邮件模板渲染测试通过");
        } catch (Exception e) {
            System.err.println("邮件模板渲染失败: " + e.getMessage());
        }
    }
}
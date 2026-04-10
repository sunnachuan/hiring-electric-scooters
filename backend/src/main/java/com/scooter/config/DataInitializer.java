package com.scooter.config;

import com.scooter.entity.Scooter;
import com.scooter.entity.User;
import com.scooter.repository.ScooterRepository;
import com.scooter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final ScooterRepository scooterRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // 检查是否已存在admin用户
        if (userRepository.findByUsername("admin").isEmpty()) {
            log.info("创建默认管理员用户...");
            
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@scooter.com");
            adminUser.setPasswordHash(passwordEncoder.encode("admin"));
            adminUser.setRole("ADMIN");
            adminUser.setIsStudent(false);
            adminUser.setIsSenior(false);
            
            userRepository.save(adminUser);
            log.info("默认管理员用户创建成功: admin/admin");
        }
        
        // 检查是否已存在普通用户
        if (userRepository.findByUsername("user").isEmpty()) {
            log.info("创建默认普通用户...");
            
            User normalUser = new User();
            normalUser.setUsername("user");
            normalUser.setEmail("user@scooter.com");
            normalUser.setPasswordHash(passwordEncoder.encode("user"));
            normalUser.setRole("USER");
            normalUser.setIsStudent(false);
            normalUser.setIsSenior(false);
            
            userRepository.save(normalUser);
        log.info("默认普通用户创建成功: user/user");
    }
    
    // 检查并创建测试滑板车数据
    if (scooterRepository.count() == 0) {
        log.info("创建测试滑板车数据...");
        
        // 城市通勤款 - 分散到3个位置
        createScooter("城市通勤款", "https://example.com/scooter1.jpg", 5, 5.0, 25.0, 1);  // 市中心广场
        createScooter("城市通勤款", "https://example.com/scooter1.jpg", 4, 5.0, 25.0, 3);  // 商业步行街
        createScooter("城市通勤款", "https://example.com/scooter1.jpg", 3, 5.0, 25.0, 5);  // 公园入口
        
        // 校园轻便款 - 分散到3个位置
        createScooter("校园轻便款", "https://example.com/scooter2.jpg", 6, 4.5, 20.0, 2);  // 大学城校区
        createScooter("校园轻便款", "https://example.com/scooter2.jpg", 2, 4.5, 20.0, 1);  // 市中心广场
        createScooter("校园轻便款", "https://example.com/scooter2.jpg", 2, 4.5, 20.0, 4);  // 地铁站出口
        
        // 商务精英款 - 分散到3个位置
        createScooter("商务精英款", "https://example.com/scooter4.jpg", 3, 6.0, 30.0, 3);  // 商业步行街
        createScooter("商务精英款", "https://example.com/scooter4.jpg", 2, 6.0, 30.0, 1);  // 市中心广场
        createScooter("商务精英款", "https://example.com/scooter4.jpg", 1, 6.0, 30.0, 2);  // 大学城校区
        
        // 时尚潮流款 - 分散到3个位置
        createScooter("时尚潮流款", "https://example.com/scooter5.jpg", 4, 5.5, 28.0, 3);  // 商业步行街
        createScooter("时尚潮流款", "https://example.com/scooter5.jpg", 2, 5.5, 28.0, 5);  // 公园入口
        createScooter("时尚潮流款", "https://example.com/scooter5.jpg", 2, 5.5, 28.0, 4);  // 地铁站出口
        
        // 休闲娱乐款 - 分散到3个位置
        createScooter("休闲娱乐款", "https://example.com/scooter7.jpg", 6, 4.0, 20.0, 5);  // 公园入口
        createScooter("休闲娱乐款", "https://example.com/scooter7.jpg", 4, 4.0, 20.0, 2);  // 大学城校区
        createScooter("休闲娱乐款", "https://example.com/scooter7.jpg", 4, 4.0, 20.0, 3);  // 商业步行街
        
        log.info("测试滑板车数据创建完成，共创建50辆滑板车");
    }
    
    log.info("数据初始化完成");
}

private void createScooter(String model, String imageUrl, int totalQuantity, double hourlyRate, double dailyRate, int locationId) {
    Scooter scooter = new Scooter();
    scooter.setModel(model);
    scooter.setImageUrl(imageUrl);
    scooter.setTotalQuantity(totalQuantity);
    scooter.setAvailableQuantity(totalQuantity);
    scooter.setHourlyRate(new BigDecimal(hourlyRate));
    scooter.setDailyRate(new BigDecimal(dailyRate));
    scooter.setStatus("AVAILABLE");
    
    // 设置点位信息
    scooter.setLocationId(locationId);
    scooter.setLocationName(getLocationName(locationId));
    scooter.setLatitude(getLocationLatitude(locationId));
    scooter.setLongitude(getLocationLongitude(locationId));
    
    scooterRepository.save(scooter);
}

private String getLocationName(int locationId) {
    switch (locationId) {
        case 1: return "市中心广场";
        case 2: return "大学城校区";
        case 3: return "商业步行街";
        case 4: return "地铁站出口";
        case 5: return "公园入口";
        default: return "点位" + locationId;
    }
}

private Double getLocationLatitude(int locationId) {
    switch (locationId) {
        case 1: return 39.9042;
        case 2: return 39.9896;
        case 3: return 39.9138;
        case 4: return 39.9022;
        case 5: return 39.9163;
        default: return 39.9042;
    }
}

private Double getLocationLongitude(int locationId) {
    switch (locationId) {
        case 1: return 116.4074;
        case 2: return 116.3509;
        case 3: return 116.3631;
        case 4: return 116.3912;
        case 5: return 116.3972;
        default: return 116.4074;
    }
}
}
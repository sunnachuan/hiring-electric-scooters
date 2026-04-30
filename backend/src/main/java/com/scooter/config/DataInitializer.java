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
import java.util.List;

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

        // 只在数据库为空时创建测试滑板车数据（保持数据持久性）
        long existingScootersCount = scooterRepository.count();
        if (existingScootersCount == 0) {
            log.info("数据库中没有滑板车数据，创建200辆测试滑板车...");
            
            // 创建200个独立的滑板车实体
            String[] models = {"城市通勤款", "校园轻便款", "商务精英款", "时尚潮流款", "休闲娱乐款"};
            String[] imageUrls = {
                "/src/assets/images/b1.png", // 城市通勤款
                "/src/assets/images/b2.png", // 校园轻便款
                "/src/assets/images/b3.png", // 商务精英款
                "/src/assets/images/b4.png", // 时尚潮流款
                "/src/assets/images/b5.png"  // 休闲娱乐款
            };
            double[] hourlyRates = {5.0, 4.5, 6.0, 5.5, 4.0};
            double[] dailyRates = {25.0, 20.0, 30.0, 28.0, 20.0};
            
            // 重新设计滑板车数据结构：随机化车辆分布，更加真实
            // 10个点位分布在北京地图范围内
            int[] allLocations = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
            
            // 随机生成每个点位的车辆数量（0-8辆，确保有1-2个点位没有车）
            int[] locationVehicleCounts = new int[10];
            int totalVehicles = 0;
            
            // 确保至少有1-2个点位没有车辆
            int emptyLocations = (int)(Math.random() * 2) + 1; // 1-2个空点位
            
            // 初始化所有点位车辆数为0
            for (int i = 0; i < 10; i++) {
                locationVehicleCounts[i] = 0;
            }
            
            // 随机分配车辆到各个点位
            while (totalVehicles < 200) {
                int locationIndex = (int)(Math.random() * 10); // 随机选择点位
                
                // 如果该点位已经有较多车辆，减少分配到该点位的概率
                if (locationVehicleCounts[locationIndex] < 10 && Math.random() > (locationVehicleCounts[locationIndex] * 0.1)) {
                    locationVehicleCounts[locationIndex]++;
                    totalVehicles++;
                }
            }
            
            // 设置1-2个点位为0辆车
            for (int i = 0; i < emptyLocations; i++) {
                int emptyLocation = (int)(Math.random() * 10);
                // 如果该点位已经有车辆，重新选择
                while (locationVehicleCounts[emptyLocation] == 0) {
                    emptyLocation = (int)(Math.random() * 10);
                }
                // 将该点位的车辆重新分配到其他点位
                int vehiclesToRedistribute = locationVehicleCounts[emptyLocation];
                locationVehicleCounts[emptyLocation] = 0;
                totalVehicles -= vehiclesToRedistribute;
                
                // 重新分配这些车辆
                for (int j = 0; j < vehiclesToRedistribute; j++) {
                    int targetLocation = (int)(Math.random() * 10);
                    // 跳过空点位
                    while (targetLocation == emptyLocation || locationVehicleCounts[targetLocation] >= 8) {
                        targetLocation = (int)(Math.random() * 10);
                    }
                    locationVehicleCounts[targetLocation]++;
                    totalVehicles++;
                }
            }
            
            log.info("随机车辆分布：");
            for (int i = 0; i < 10; i++) {
                log.info("点位{}：{}辆车", i + 1, locationVehicleCounts[i]);
            }
            
            int scooterId = 1;
            
            // 为每个点位创建车辆，按照随机分布方案
            for (int locationId = 1; locationId <= 10; locationId++) {
                int vehicleCount = locationVehicleCounts[locationId - 1];
                
                // 如果该点位没有车辆，跳过
                if (vehicleCount == 0) {
                    continue;
                }
                
                // 为该点位创建车辆，随机分配车型
                for (int i = 0; i < vehicleCount; i++) {
                    // 随机选择车型
                    int modelIndex = (int)(Math.random() * models.length);
                    
                    // 创建独立的滑板车实体
                    Scooter scooter = new Scooter();
                    scooter.setModel(models[modelIndex]);
                    scooter.setImageUrl(imageUrls[modelIndex]);
                    scooter.setTotalQuantity(1); // 每个实体代表一辆车
                    scooter.setAvailableQuantity(1);
                    scooter.setHourlyRate(new BigDecimal(hourlyRates[modelIndex]));
                    scooter.setDailyRate(new BigDecimal(dailyRates[modelIndex]));
                    scooter.setStatus("AVAILABLE");
                    
                    // 设置点位信息
                    scooter.setLocationId(locationId);
                    scooter.setLocationName(getLocationName(locationId));
                    scooter.setLatitude(getLocationLatitude(locationId));
                    scooter.setLongitude(getLocationLongitude(locationId));
                    
                    // 设置设备状态（更真实的数据）
                    scooter.setBatteryLevel(20.0 + Math.random() * 80.0); // 20-100%电量
                    scooter.setTotalMileage(Math.random() * 5000.0); // 0-5000公里
                    scooter.setIsOnline(Math.random() > 0.15); // 85%在线率
                    scooter.setIsLocked(true); // 所有设备初始状态为锁定
                    
                    scooterRepository.save(scooter);
                    scooterId++;
                }
            }
            
            // 验证车辆创建情况
            long totalScooters = scooterRepository.count();
            log.info("测试滑板车数据创建完成，共创建{}辆滑板车", totalScooters);
            
            // 检查每个点位的车辆分布
            for (int i = 1; i <= 10; i++) {
                List<Scooter> scootersAtLocation = scooterRepository.findByLocationId(i);
                log.info("点位{}（{}）有{}辆滑板车", i, getLocationName(i), scootersAtLocation.size());
            }
            
            log.info("数据初始化完成");
        } else {
            log.info("数据库中已有{}辆滑板车，保持现有数据不变", existingScootersCount);
        }
    }
    
    private String getLocationName(int locationId) {
        switch (locationId) {
            case 1: return "市中心广场";
            case 2: return "大学城校区";
            case 3: return "商业步行街";
            case 4: return "地铁站出口";
            case 5: return "公园入口";
            case 6: return "火车站北广场";
            case 7: return "科技园区";
            case 8: return "体育中心";
            case 9: return "购物中心";
            case 10: return "医院门口";
            default: return "点位" + locationId;
        }
    }
    
    private Double getLocationLatitude(int locationId) {
        switch (locationId) {
            case 1: return 39.9042;   // 天安门广场
            case 2: return 39.9924;   // 北京大学东门
            case 3: return 39.9141;   // 王府井步行街
            case 4: return 39.9078;   // 西单地铁站
            case 5: return 39.9999;   // 颐和园东门
            case 6: return 39.9834;   // 中关村科技园
            case 7: return 39.9334;   // 朝阳公园
            case 8: return 39.9096;   // 国贸CBD
            case 9: return 39.9334;   // 三里屯太古里
            case 10: return 39.9927;  // 鸟巢水立方
            default: return 39.9042;
        }
    }
    
    private Double getLocationLongitude(int locationId) {
        switch (locationId) {
            case 1: return 116.4074;  // 天安门广场
            case 2: return 116.3163;  // 北京大学东门
            case 3: return 116.4079;  // 王府井步行街
            case 4: return 116.3732;  // 西单地铁站
            case 5: return 116.2735;  // 颐和园东门
            case 6: return 116.3164;  // 中关村科技园
            case 7: return 116.4833;  // 朝阳公园
            case 8: return 116.4580;  // 国贸CBD
            case 9: return 116.4533;  // 三里屯太古里
            case 10: return 116.3963; // 鸟巢水立方
            default: return 116.4074;
        }
    }
}
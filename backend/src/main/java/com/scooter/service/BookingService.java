package com.scooter.service;

import com.scooter.entity.BankCard;
import com.scooter.entity.Booking;
import com.scooter.entity.Payment;
import com.scooter.entity.Scooter;
import com.scooter.entity.User;
import com.scooter.repository.BankCardRepository;
import com.scooter.repository.BookingRepository;
import com.scooter.repository.PaymentRepository;
import com.scooter.repository.ScooterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookingService {
    
    private final BookingRepository bookingRepository;
    private final ScooterRepository scooterRepository;
    private final PaymentRepository paymentRepository;
    private final BankCardRepository bankCardRepository;
    private final BankCardService bankCardService;
    private final EmailService emailService;
    private final ScooterService scooterService;
    
    @Transactional
    public Booking createBooking(User user, Scooter scooter, Integer hours, String cardNumber, Long bankCardId) {
        // 检查滑板车是否可用
        if (!"AVAILABLE".equals(scooter.getStatus())) {
            throw new RuntimeException("滑板车不可用");
        }
        
        // 计算开始和结束时间
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(hours);
        
        // 检查时间冲突
        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                scooter.getId(), startTime, endTime);
        if (!overlappingBookings.isEmpty()) {
            throw new RuntimeException("该时间段内滑板车已被预订");
        }
        
        // 计算价格和折扣
        BigDecimal basePrice = calculateTieredPrice(scooter.getHourlyRate(), hours);
        BigDecimal discount = calculateDiscount(user);
        BigDecimal totalPrice = basePrice.multiply(discount);
        
        // 创建预订
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setScooter(scooter);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setDurationType(hours + "h");
        booking.setTotalPrice(totalPrice);
        booking.setDiscountApplied(discount);
        booking.setStatus("PENDING");
        
        // 先保存预订以获取ID
        Booking savedBooking = bookingRepository.save(booking);
        
        // 处理支付（使用已保存的预订）
        processPayment(savedBooking, cardNumber, bankCardId);
        
        // 更新滑板车可用数量
        scooterService.decrementAvailableQuantity(scooter.getId());
        
        // 发送预订确认邮件（可选，失败不影响主要流程）
        try {
            sendBookingConfirmationEmail(savedBooking, user);
            // emailService.sendPaymentConfirmation(savedBooking, user); // 暂时注释掉，需要实现
        } catch (Exception e) {
            // 邮件发送失败不应影响主要业务流程
            System.err.println("邮件发送失败（不影响预订）: " + e.getMessage());
        }
        
        return savedBooking;
    }
    

    
    private BigDecimal calculateTieredPrice(BigDecimal hourlyRate, Integer hours) {
        BigDecimal basePrice = hourlyRate; // 基准单价P
        
        // 分层定价逻辑
        if (hours <= 3) {
            // 1-3小时：100% 原价
            return basePrice.multiply(BigDecimal.valueOf(hours));
        } else if (hours <= 8) {
            // 4-8小时：85% 折扣
            return basePrice.multiply(BigDecimal.valueOf(hours)).multiply(BigDecimal.valueOf(0.85));
        } else if (hours <= 24) {
            // 9-24小时：60% 折扣，但最高收12小时费用
            int effectiveHours = Math.min(hours, 12);
            return basePrice.multiply(BigDecimal.valueOf(effectiveHours)).multiply(BigDecimal.valueOf(0.6));
        } else if (hours <= 72) {
            // 1-3天：50% 折扣，每天按12小时计费
            int days = (int) Math.ceil(hours / 24.0);
            return basePrice.multiply(BigDecimal.valueOf(12 * days)).multiply(BigDecimal.valueOf(0.5));
        } else {
            // 3天以上：30% 折扣，每天按12小时计费
            int days = (int) Math.ceil(hours / 24.0);
            return basePrice.multiply(BigDecimal.valueOf(12 * days)).multiply(BigDecimal.valueOf(0.3));
        }
    }
    
    private BigDecimal calculateDiscount(User user) {
        BigDecimal discount = BigDecimal.ONE;
        
        // 检查频繁用户折扣（7天内租赁≥8小时）
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        List<Booking> recentBookings = bookingRepository.findCompletedBookingsByUserAndDateRange(
                user, weekAgo, LocalDateTime.now());
        
        long totalHours = recentBookings.stream()
                .mapToLong(b -> ChronoUnit.HOURS.between(b.getStartTime(), b.getEndTime()))
                .sum();
        
        if (totalHours >= 8) {
            discount = BigDecimal.valueOf(0.9); // 9折
        }
        
        // 检查学生/老年人折扣
        if (user.getIsStudent() || user.getIsSenior()) {
            BigDecimal specialDiscount = BigDecimal.valueOf(0.95); // 9.5折
            if (specialDiscount.compareTo(discount) < 0) {
                discount = specialDiscount;
            }
        }
        
        return discount;
    }
    
    private void processPayment(Booking booking, String cardNumber, Long bankCardId) {
        // 简化支付验证 - 模拟支付，只需基本验证
        String finalCardNumber = null;
        
        if (bankCardId != null) {
            // 使用存储的银行卡
            BankCard bankCard = bankCardRepository.findById(bankCardId)
                    .orElseThrow(() -> new RuntimeException("银行卡不存在"));
            
            if (!bankCard.getUser().getId().equals(booking.getUser().getId())) {
                throw new RuntimeException("无权使用此银行卡");
            }
            
            finalCardNumber = bankCard.getCardNumber();
        } else if (cardNumber != null && !cardNumber.trim().isEmpty()) {
            // 使用直接输入的卡号
            finalCardNumber = cardNumber;
        } else {
            // 如果没有提供支付信息，使用模拟卡号
            finalCardNumber = "123456789012";
        }
        
        // 简化验证：只需基本非空检查
        if (finalCardNumber == null || finalCardNumber.trim().isEmpty()) {
            throw new RuntimeException("支付失败：无效的支付信息");
        }
        
        // 模拟支付成功
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalPrice());
        
        // 安全处理卡号显示
        if (finalCardNumber.length() >= 4) {
            payment.setCardLastFour(finalCardNumber.substring(finalCardNumber.length() - 4));
        } else {
            payment.setCardLastFour("1234");
        }
        
        paymentRepository.save(payment);
        booking.setStatus("ACTIVE");
    }
    
    public List<Booking> getUserBookings(Long userId) {
        return bookingRepository.findByUserId(userId);
    }
    
    public List<Booking> getUserActiveBookings(Long userId) {
        return bookingRepository.findByUserIdAndStatusIn(userId, List.of("PENDING", "ACTIVE"));
    }
    
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }
    
    @Transactional
    public Booking cancelBooking(Long bookingId, User user) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("预订不存在"));
        
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权取消此预订");
        }
        
        if (!"PENDING".equals(booking.getStatus())) {
            throw new RuntimeException("只能取消未开始的预订");
        }
        
        booking.setStatus("CANCELLED");
        
        // 恢复滑板车可用数量
        scooterService.incrementAvailableQuantity(booking.getScooter().getId());
        
        Booking cancelledBooking = bookingRepository.save(booking);
        
        // 发送预订取消邮件
        try {
            emailService.sendBookingCancellation(cancelledBooking, user);
        } catch (Exception e) {
            // 邮件发送失败不应影响主要业务流程
            System.err.println("取消预订邮件发送失败: " + e.getMessage());
        }
        
        return cancelledBooking;
    }
    
    /**
     * 提前还车
     */
    @Transactional
    public Booking returnScooterEarly(Long bookingId, User user) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("预订不存在"));
        
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权操作此预订");
        }
        
        if (!"ACTIVE".equals(booking.getStatus())) {
            throw new RuntimeException("只能归还进行中的预订");
        }
        
        // 更新预订状态为已完成
        booking.setStatus("COMPLETED");
        booking.setEndTime(LocalDateTime.now());
        
        // 恢复滑板车可用数量
        scooterService.incrementAvailableQuantity(booking.getScooter().getId());
        
        Booking returnedBooking = bookingRepository.save(booking);
        
        // 发送还车确认邮件
        try {
            emailService.sendReturnConfirmation(returnedBooking, user);
        } catch (Exception e) {
            // 邮件发送失败不应影响主要业务流程
            System.err.println("还车确认邮件发送失败: " + e.getMessage());
        }
        
        return returnedBooking;
    }
    
    @Transactional
    public Booking extendBooking(Long bookingId, Integer hours, User user) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("预订不存在"));
        
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权延长此预订");
        }
        
        if (!"ACTIVE".equals(booking.getStatus())) {
            throw new RuntimeException("只能延长进行中的预订");
        }
        
        // 确保最少延长1小时
        if (hours < 1) {
            throw new RuntimeException("延长时间不能少于1小时");
        }
        
        // 计算延长价格（直接使用分层定价，不应用额外折扣）
        BigDecimal extensionPrice = calculateTieredPrice(booking.getScooter().getHourlyRate(), hours);
        
        // 更新结束时间和总价
        LocalDateTime newEndTime = booking.getEndTime().plusHours(hours);
        booking.setEndTime(newEndTime);
        booking.setTotalPrice(booking.getTotalPrice().add(extensionPrice));
        
        Booking extendedBooking = bookingRepository.save(booking);
        
        // 发送预订延长邮件
        try {
            emailService.sendBookingExtension(extendedBooking, user);
        } catch (Exception e) {
            // 邮件发送失败不应影响主要业务流程
            System.err.println("延长预订邮件发送失败: " + e.getMessage());
        }
        
        return extendedBooking;
    }
    
    /**
     * 计算从指定时间开始的总收入
     */
    public Double calculateTotalRevenueSince(LocalDateTime startDate) {
        return bookingRepository.calculateTotalRevenueSince(startDate);
    }
    
    /**
     * 获取按租用时长分类的收入统计
     */
    public Map<String, Double> getRevenueByDurationTypeSince(LocalDateTime startDate) {
        List<Object[]> results = bookingRepository.findRevenueByDurationTypeSince(startDate);
        Map<String, Double> revenueByDuration = new HashMap<>();
        
        // 初始化所有可能的租用时长类型
        revenueByDuration.put("1h", 0.0);
        revenueByDuration.put("4h", 0.0);
        revenueByDuration.put("1d", 0.0);
        revenueByDuration.put("1w", 0.0);
        
        // 填充实际数据
        for (Object[] result : results) {
            String durationType = (String) result[0];
            Double revenue = (Double) result[1];
            if (revenue != null) {
                revenueByDuration.put(durationType, revenue);
            }
        }
        
        return revenueByDuration;
    }
    
    /**
     * 获取每日收入统计
     */
    public Map<String, Double> getDailyRevenueSince(LocalDateTime startDate) {
        List<Object[]> results = bookingRepository.findDailyRevenueSince(startDate);
        Map<String, Double> dailyRevenue = new HashMap<>();
        
        // 生成过去7天的日期
        for (int i = 6; i >= 0; i--) {
            LocalDateTime date = LocalDateTime.now().minusDays(i);
            String dateKey = date.toLocalDate().toString();
            dailyRevenue.put(dateKey, 0.0);
        }
        
        // 填充实际数据
        for (Object[] result : results) {
            try {
                LocalDateTime startTime = (LocalDateTime) result[0];
                Double revenue = (Double) result[1];
                if (revenue != null && startTime != null) {
                    String dateKey = startTime.toLocalDate().toString();
                    dailyRevenue.put(dateKey, dailyRevenue.getOrDefault(dateKey, 0.0) + revenue);
                }
            } catch (Exception e) {
                // 如果类型转换失败，使用默认值
                System.err.println("Error processing daily revenue data: " + e.getMessage());
            }
        }
        
        return dailyRevenue;
    }
    
    /**
     * 为临时用户创建预订
     */
    @Transactional
    public Booking createTemporaryUserBooking(com.scooter.entity.TemporaryUser temporaryUser, Scooter scooter, Integer hours) {
        // 检查滑板车是否可用
        if (!"AVAILABLE".equals(scooter.getStatus())) {
            throw new RuntimeException("滑板车不可用");
        }
        
        // 计算开始和结束时间
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(hours);
        
        // 检查时间冲突
        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                scooter.getId(), startTime, endTime);
        if (!overlappingBookings.isEmpty()) {
            throw new RuntimeException("该时间段内滑板车已被预订");
        }
        
        // 计算价格（使用分层定价）
        BigDecimal totalPrice = calculateTieredPrice(scooter.getHourlyRate(), hours);
        
        // 创建预订
        Booking booking = new Booking();
        booking.setScooter(scooter);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setTotalPrice(totalPrice);
        booking.setStatus("ACTIVE");
        
        // 设置临时用户信息（特殊处理）
        booking.setTemporaryUserId(temporaryUser.getId());
        booking.setUserType("TEMPORARY");
        booking.setUserInfo(temporaryUser.getRealName() + " (临时用户)");
        
        // 保存预订
        Booking savedBooking = bookingRepository.save(booking);
        
        // 减少滑板车可用数量
        scooterService.decrementAvailableQuantity(scooter.getId());
        
        // 创建支付记录（使用临时用户的银行卡）
        Payment payment = new Payment();
        payment.setBooking(savedBooking);
        payment.setAmount(totalPrice);
        
        // 设置银行卡信息
        if (temporaryUser.getBankCard() != null) {
            payment.setCardLastFour(temporaryUser.getBankCard().getCardNumberDisplay().substring(temporaryUser.getBankCard().getCardNumberDisplay().length() - 4));
        } else {
            payment.setCardLastFour("1234"); // 默认值
        }
        
        paymentRepository.save(payment);
        
        return savedBooking;
    }
    
    /**
     * 发送预订确认邮件
     */
    private void sendBookingConfirmationEmail(Booking booking, User user) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String startTime = booking.getStartTime().format(formatter);
            String endTime = booking.getEndTime().format(formatter);
            
            emailService.sendBookingConfirmation(
                user.getEmail(),
                user.getUsername(),
                booking.getId().toString(),
                booking.getScooter().getModel(),
                startTime,
                endTime,
                booking.getTotalPrice()
            );
        } catch (Exception e) {
            throw new RuntimeException("发送预订确认邮件失败: " + e.getMessage());
        }
    }
}
package com.scooter.service;

import com.scooter.entity.Booking;
import com.scooter.entity.Payment;
import com.scooter.entity.Scooter;
import com.scooter.entity.User;
import com.scooter.repository.BookingRepository;
import com.scooter.repository.PaymentRepository;
import com.scooter.repository.ScooterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {
    
    private final BookingRepository bookingRepository;
    private final ScooterRepository scooterRepository;
    private final PaymentRepository paymentRepository;
    private final EmailService emailService;
    
    @Transactional
    public Booking createBooking(User user, Scooter scooter, String durationType, String cardNumber) {
        // 检查滑板车是否可用
        if (!"AVAILABLE".equals(scooter.getStatus())) {
            throw new RuntimeException("滑板车不可用");
        }
        
        // 计算开始和结束时间
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = calculateEndTime(startTime, durationType);
        
        // 检查时间冲突
        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                scooter.getId(), startTime, endTime);
        if (!overlappingBookings.isEmpty()) {
            throw new RuntimeException("该时间段内滑板车已被预订");
        }
        
        // 计算价格和折扣
        BigDecimal basePrice = calculateBasePrice(scooter, durationType);
        BigDecimal discount = calculateDiscount(user);
        BigDecimal totalPrice = basePrice.multiply(discount);
        
        // 创建预订
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setScooter(scooter);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setDurationType(durationType);
        booking.setTotalPrice(totalPrice);
        booking.setDiscountApplied(discount);
        booking.setStatus("PENDING");
        
        // 处理支付
        processPayment(booking, cardNumber);
        
        // 更新滑板车可用数量
        scooterService.decrementAvailableQuantity(scooter.getId());
        
        Booking savedBooking = bookingRepository.save(booking);
        
        // 发送预订确认邮件
        try {
            emailService.sendBookingConfirmation(savedBooking, user);
            emailService.sendPaymentConfirmation(savedBooking, user);
        } catch (Exception e) {
            // 邮件发送失败不应影响主要业务流程
            System.err.println("邮件发送失败: " + e.getMessage());
        }
        
        return savedBooking;
    }
    
    private LocalDateTime calculateEndTime(LocalDateTime startTime, String durationType) {
        switch (durationType) {
            case "1h":
                return startTime.plusHours(1);
            case "4h":
                return startTime.plusHours(4);
            case "1d":
                return startTime.plusDays(1);
            case "1w":
                return startTime.plusWeeks(1);
            default:
                throw new RuntimeException("无效的租赁时长");
        }
    }
    
    private BigDecimal calculateBasePrice(Scooter scooter, String durationType) {
        switch (durationType) {
            case "1h":
                return scooter.getHourlyRate();
            case "4h":
                return scooter.getHourlyRate().multiply(BigDecimal.valueOf(4));
            case "1d":
                return scooter.getDailyRate();
            case "1w":
                return scooter.getDailyRate().multiply(BigDecimal.valueOf(7));
            default:
                throw new RuntimeException("无效的租赁时长");
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
    
    private void processPayment(Booking booking, String cardNumber) {
        // 模拟支付验证
        if (cardNumber == null || cardNumber.length() < 12) {
            throw new RuntimeException("支付失败：无效的信用卡号");
        }
        
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalPrice());
        payment.setCardLastFour(cardNumber.substring(cardNumber.length() - 4));
        
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
    
    @Transactional
    public Booking extendBooking(Long bookingId, String durationType, User user) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("预订不存在"));
        
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权延长此预订");
        }
        
        if (!"ACTIVE".equals(booking.getStatus())) {
            throw new RuntimeException("只能延长进行中的预订");
        }
        
        // 计算延长价格
        BigDecimal extensionPrice = calculateBasePrice(booking.getScooter(), durationType)
                .multiply(booking.getDiscountApplied());
        
        // 更新结束时间和总价
        LocalDateTime newEndTime = calculateEndTime(booking.getEndTime(), durationType);
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
}
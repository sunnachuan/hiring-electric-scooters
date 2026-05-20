package com.scooter.service;

import com.scooter.entity.Booking;
import com.scooter.entity.Scooter;
import com.scooter.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private DeviceService deviceService;

    @InjectMocks
    private BillingService billingService;

    private Booking testBooking;
    private Scooter testScooter;

    @BeforeEach
    void setUp() {
        testScooter = new Scooter();
        testScooter.setId(1L);
        testScooter.setModel("测试滑板车");
        testScooter.setHourlyRate(new BigDecimal("20.00"));

        testBooking = new Booking();
        testBooking.setId(1L);
        testBooking.setScooter(testScooter);
        testBooking.setStartTime(LocalDateTime.now().minusHours(2));
        testBooking.setEndTime(LocalDateTime.now());
        testBooking.setBillingType("TIME_DISTANCE");
        testBooking.setTimeRate(new BigDecimal("20.00"));
        testBooking.setDistanceRate(new BigDecimal("0.50"));
        testBooking.setDiscountApplied(BigDecimal.ONE);
        testBooking.setDistanceTraveled(new BigDecimal("10.00"));
    }

    @Test
    void testCalculateTotalFee_TimeOnly() {
        testBooking.setBillingType("TIME_ONLY");
        testBooking.setDistanceTraveled(BigDecimal.ZERO);

        BigDecimal fee = billingService.calculateTotalFee(testBooking);

        assertEquals(new BigDecimal("40.00"), fee);
    }

    @Test
    void testCalculateTotalFee_DistanceOnly() {
        testBooking.setBillingType("DISTANCE_ONLY");

        BigDecimal fee = billingService.calculateTotalFee(testBooking);

        assertEquals(new BigDecimal("5.00"), fee);
    }

    @Test
    void testCalculateTotalFee_TimeAndDistance() {
        testBooking.setBillingType("TIME_DISTANCE");

        BigDecimal fee = billingService.calculateTotalFee(testBooking);

        assertEquals(new BigDecimal("45.00"), fee);
    }

    @Test
    void testCalculateTotalFee_WithDiscount() {
        testBooking.setBillingType("TIME_ONLY");
        testBooking.setDistanceTraveled(BigDecimal.ZERO);
        testBooking.setDiscountApplied(new BigDecimal("0.90"));

        BigDecimal fee = billingService.calculateTotalFee(testBooking);

        assertEquals(new BigDecimal("36.00"), fee);
    }

    @Test
    void testCalculateTotalFee_DistanceOnly_ZeroDistance() {
        testBooking.setBillingType("DISTANCE_ONLY");
        testBooking.setDistanceTraveled(BigDecimal.ZERO);

        BigDecimal fee = billingService.calculateTotalFee(testBooking);

        assertEquals(BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP), fee);
    }

    @Test
    void testGetTodayRevenue() {
        when(bookingRepository.calculateRevenueBetween(any(), any()))
                .thenReturn(Optional.of(new BigDecimal("500.00")));

        BigDecimal revenue = billingService.getTodayRevenue();

        assertEquals(new BigDecimal("500.00"), revenue);
    }

    @Test
    void testGetWeeklyRevenue() {
        when(bookingRepository.calculateRevenueBetween(any(), any()))
                .thenReturn(Optional.of(new BigDecimal("3500.00")));

        BigDecimal revenue = billingService.getWeeklyRevenue();

        assertEquals(new BigDecimal("3500.00"), revenue);
    }
}
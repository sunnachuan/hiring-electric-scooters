package com.scooter.service;

import com.scooter.entity.Booking;
import com.scooter.entity.Scooter;
import com.scooter.entity.User;
import com.scooter.repository.BankCardRepository;
import com.scooter.repository.BookingRepository;
import com.scooter.repository.PaymentRepository;
import com.scooter.repository.ScooterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ScooterRepository scooterRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BankCardRepository bankCardRepository;

    @Mock
    private BankCardService bankCardService;

    @Mock
    private EmailService emailService;

    @Mock
    private ScooterService scooterService;

    @InjectMocks
    private BookingService bookingService;

    private User testUser;
    private Scooter testScooter;
    private Booking testBooking;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRole("USER");
        testUser.setIsStudent(false);
        testUser.setIsSenior(false);

        testScooter = new Scooter();
        testScooter.setId(1L);
        testScooter.setModel("测试滑板车");
        testScooter.setHourlyRate(new BigDecimal("20.00"));
        testScooter.setStatus("AVAILABLE");
        testScooter.setAvailableQuantity(5);

        testBooking = new Booking();
        testBooking.setId(1L);
        testBooking.setUser(testUser);
        testBooking.setScooter(testScooter);
        testBooking.setStartTime(LocalDateTime.now());
        testBooking.setEndTime(LocalDateTime.now().plusHours(2));
        testBooking.setTotalPrice(new BigDecimal("40.00"));
        testBooking.setStatus("ACTIVE");
    }

    @Test
    void testCreateBooking_Success() {
        when(bookingRepository.findOverlappingBookings(anyLong(), any(), any())).thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Booking booking = bookingService.createBooking(testUser, testScooter, 2, "123456789012", null);

        assertNotNull(booking);
        assertEquals("ACTIVE", booking.getStatus());
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(paymentRepository, times(1)).save(any());
        verify(scooterService, times(1)).decrementAvailableQuantity(testScooter.getId());
    }

    @Test
    void testCreateBooking_ScooterNotAvailable() {
        testScooter.setStatus("UNAVAILABLE");

        assertThrows(RuntimeException.class, () -> 
            bookingService.createBooking(testUser, testScooter, 2, "123456789012", null));
    }

    @Test
    void testCreateBooking_OverlappingBookings() {
        when(bookingRepository.findOverlappingBookings(anyLong(), any(), any())).thenReturn(List.of(testBooking));

        assertThrows(RuntimeException.class, () -> 
            bookingService.createBooking(testUser, testScooter, 2, "123456789012", null));
    }

    @Test
    void testGetUserBookings() {
        when(bookingRepository.findByUserId(1L)).thenReturn(List.of(testBooking));

        List<Booking> bookings = bookingService.getUserBookings(1L);

        assertFalse(bookings.isEmpty());
        assertEquals(1, bookings.size());
    }

    @Test
    void testGetUserActiveBookings() {
        when(bookingRepository.findByUserIdAndStatusIn(1L, List.of("PENDING", "ACTIVE"))).thenReturn(List.of(testBooking));

        List<Booking> bookings = bookingService.getUserActiveBookings(1L);

        assertFalse(bookings.isEmpty());
        assertEquals(1, bookings.size());
    }

    @Test
    void testGetAllBookings() {
        when(bookingRepository.findAll()).thenReturn(List.of(testBooking));

        List<Booking> bookings = bookingService.getAllBookings();

        assertFalse(bookings.isEmpty());
        assertEquals(1, bookings.size());
    }

    @Test
    void testGetActiveBookingsCount() {
        when(bookingRepository.countByStatusIn(List.of("PENDING", "ACTIVE"))).thenReturn(5);

        int count = bookingService.getActiveBookingsCount();

        assertEquals(5, count);
    }

    @Test
    void testCancelBooking_Success() {
        testBooking.setStatus("PENDING");
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking cancelled = bookingService.cancelBooking(1L, testUser);

        assertNotNull(cancelled);
        assertEquals("CANCELLED", cancelled.getStatus());
        verify(scooterService, times(1)).incrementAvailableQuantity(testScooter.getId());
    }

    @Test
    void testCancelBooking_NotFound() {
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            bookingService.cancelBooking(999L, testUser));
    }

    @Test
    void testCancelBooking_NotOwner() {
        User otherUser = new User();
        otherUser.setId(2L);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        assertThrows(RuntimeException.class, () -> 
            bookingService.cancelBooking(1L, otherUser));
    }

    @Test
    void testCancelBooking_NotPending() {
        testBooking.setStatus("ACTIVE");
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        assertThrows(RuntimeException.class, () -> 
            bookingService.cancelBooking(1L, testUser));
    }

    @Test
    void testReturnScooterEarly_Success() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking returned = bookingService.returnScooterEarly(1L, testUser);

        assertNotNull(returned);
        assertEquals("COMPLETED", returned.getStatus());
        verify(scooterService, times(1)).incrementAvailableQuantity(testScooter.getId());
    }

    @Test
    void testReturnScooterEarly_NotActive() {
        testBooking.setStatus("PENDING");
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        assertThrows(RuntimeException.class, () -> 
            bookingService.returnScooterEarly(1L, testUser));
    }

    @Test
    void testExtendBooking_Success() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking extended = bookingService.extendBooking(1L, 2, testUser);

        assertNotNull(extended);
        assertTrue(extended.getTotalPrice().compareTo(new BigDecimal("40.00")) > 0);
    }

    @Test
    void testExtendBooking_TooFewHours() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        assertThrows(RuntimeException.class, () -> 
            bookingService.extendBooking(1L, 0, testUser));
    }

    @Test
    void testCalculateTotalRevenueSince() {
        when(bookingRepository.calculateTotalRevenueSince(any())).thenReturn(new BigDecimal("1000.00"));

        BigDecimal revenue = bookingService.calculateTotalRevenueSince(LocalDateTime.now().minusDays(7));

        assertEquals(new BigDecimal("1000.00"), revenue);
    }

    @Test
    void testGetDailyRevenueSince() {
        when(bookingRepository.findDailyRevenueSince(any())).thenReturn(Collections.emptyList());

        var revenueMap = bookingService.getDailyRevenueSince(LocalDateTime.now().minusDays(7));

        assertNotNull(revenueMap);
        assertEquals(7, revenueMap.size());
    }

    // ======================== 分层定价测试 ========================

    @Test
    void testCreateBooking_TieredPrice_1hTo3h() {
        when(bookingRepository.findOverlappingBookings(anyLong(), any(), any())).thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Booking booking = bookingService.createBooking(testUser, testScooter, 2, "123456789012", null);

        assertEquals(0, booking.getTotalPrice().compareTo(new BigDecimal("40.00")));
    }

    @Test
    void testCreateBooking_TieredPrice_4hTo8h() {
        when(bookingRepository.findOverlappingBookings(anyLong(), any(), any())).thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Booking booking = bookingService.createBooking(testUser, testScooter, 5, "123456789012", null);

        assertEquals(0, booking.getTotalPrice().compareTo(new BigDecimal("85.00")));
    }

    @Test
    void testCreateBooking_TieredPrice_9hTo24h() {
        when(bookingRepository.findOverlappingBookings(anyLong(), any(), any())).thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Booking booking = bookingService.createBooking(testUser, testScooter, 12, "123456789012", null);

        assertEquals(0, booking.getTotalPrice().compareTo(new BigDecimal("144.00")));
    }

    @Test
    void testCreateBooking_TieredPrice_1dTo3d() {
        when(bookingRepository.findOverlappingBookings(anyLong(), any(), any())).thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Booking booking = bookingService.createBooking(testUser, testScooter, 48, "123456789012", null);

        assertEquals(0, booking.getTotalPrice().compareTo(new BigDecimal("240.00")));
    }

    @Test
    void testCreateBooking_TieredPrice_Over3d() {
        when(bookingRepository.findOverlappingBookings(anyLong(), any(), any())).thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Booking booking = bookingService.createBooking(testUser, testScooter, 96, "123456789012", null);

        assertEquals(0, booking.getTotalPrice().compareTo(new BigDecimal("288.00")));
    }

    // ======================== 折扣计算测试 ========================

    @Test
    void testCreateBooking_FrequentUserDiscount() {
        Booking recentBooking = new Booking();
        recentBooking.setStartTime(LocalDateTime.now().minusDays(1).minusHours(9));
        recentBooking.setEndTime(LocalDateTime.now().minusDays(1));

        when(bookingRepository.findOverlappingBookings(anyLong(), any(), any())).thenReturn(Collections.emptyList());
        when(bookingRepository.findCompletedBookingsByUserAndDateRange(any(), any(), any()))
                .thenReturn(List.of(recentBooking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Booking booking = bookingService.createBooking(testUser, testScooter, 2, "123456789012", null);

        assertEquals(0, booking.getDiscountApplied().compareTo(new BigDecimal("0.90")));
        assertEquals(0, booking.getTotalPrice().compareTo(new BigDecimal("36.00")));
    }

    @Test
    void testCreateBooking_StudentDiscount() {
        testUser.setIsStudent(true);

        when(bookingRepository.findOverlappingBookings(anyLong(), any(), any())).thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Booking booking = bookingService.createBooking(testUser, testScooter, 2, "123456789012", null);

        assertEquals(0, booking.getDiscountApplied().compareTo(new BigDecimal("0.95")));
        assertEquals(0, booking.getTotalPrice().compareTo(new BigDecimal("38.00")));
    }

    @Test
    void testCreateBooking_SeniorDiscount() {
        testUser.setIsSenior(true);

        when(bookingRepository.findOverlappingBookings(anyLong(), any(), any())).thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Booking booking = bookingService.createBooking(testUser, testScooter, 2, "123456789012", null);

        assertEquals(0, booking.getDiscountApplied().compareTo(new BigDecimal("0.95")));
        assertEquals(0, booking.getTotalPrice().compareTo(new BigDecimal("38.00")));
    }

    @Test
    void testCreateBooking_FrequentAndStudent_DiscountTakesMinimum() {
        testUser.setIsStudent(true);

        Booking recentBooking = new Booking();
        recentBooking.setStartTime(LocalDateTime.now().minusDays(1).minusHours(9));
        recentBooking.setEndTime(LocalDateTime.now().minusDays(1));

        when(bookingRepository.findOverlappingBookings(anyLong(), any(), any())).thenReturn(Collections.emptyList());
        when(bookingRepository.findCompletedBookingsByUserAndDateRange(any(), any(), any()))
                .thenReturn(List.of(recentBooking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Booking booking = bookingService.createBooking(testUser, testScooter, 2, "123456789012", null);

        assertEquals(0, booking.getDiscountApplied().compareTo(new BigDecimal("0.90")));
        assertEquals(0, booking.getTotalPrice().compareTo(new BigDecimal("36.00")));
    }

    // ======================== 临时用户预订测试 ========================

    @Test
    void testCreateTemporaryUserBooking_Success() {
        com.scooter.entity.TemporaryUser tempUser = new com.scooter.entity.TemporaryUser();
        tempUser.setId(1L);
        tempUser.setRealName("临时用户");
        tempUser.setPhone("13800138000");

        when(bookingRepository.findOverlappingBookings(anyLong(), any(), any())).thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Booking booking = bookingService.createTemporaryUserBooking(tempUser, testScooter, 2);

        assertNotNull(booking);
        assertEquals("ACTIVE", booking.getStatus());
        assertEquals("TEMPORARY", booking.getUserType());
        assertEquals(Long.valueOf(1L), booking.getTemporaryUserId());
        verify(scooterService, times(1)).decrementAvailableQuantity(testScooter.getId());
    }

    @Test
    void testCreateTemporaryUserBooking_ScooterNotAvailable() {
        testScooter.setStatus("UNAVAILABLE");
        com.scooter.entity.TemporaryUser tempUser = new com.scooter.entity.TemporaryUser();
        tempUser.setId(1L);

        assertThrows(RuntimeException.class, () ->
                bookingService.createTemporaryUserBooking(tempUser, testScooter, 2));
    }

    @Test
    void testCreateTemporaryUserBooking_Overlapping() {
        com.scooter.entity.TemporaryUser tempUser = new com.scooter.entity.TemporaryUser();
        tempUser.setId(1L);
        when(bookingRepository.findOverlappingBookings(anyLong(), any(), any())).thenReturn(List.of(testBooking));

        assertThrows(RuntimeException.class, () ->
                bookingService.createTemporaryUserBooking(tempUser, testScooter, 2));
    }
}

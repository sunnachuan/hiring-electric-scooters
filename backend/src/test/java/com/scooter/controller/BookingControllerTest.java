package com.scooter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scooter.dto.BookingRequest;
import com.scooter.entity.Booking;
import com.scooter.entity.Scooter;
import com.scooter.entity.User;
import com.scooter.service.BookingService;
import com.scooter.service.ScooterService;
import com.scooter.util.SecurityUtils;
import com.scooter.config.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private ScooterService scooterService;

    @MockBean
    private SecurityUtils securityUtils;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private com.scooter.service.UserService userService;

    @MockBean
    private com.scooter.service.SecurityAuditService securityAuditService;

    @MockBean
    private com.scooter.service.EmailService emailService;

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

        testScooter = new Scooter();
        testScooter.setId(1L);
        testScooter.setModel("测试滑板车");
        testScooter.setHourlyRate(new BigDecimal("20.00"));
        testScooter.setStatus("AVAILABLE");

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
    @WithMockUser(username = "testuser")
    void testCreateBooking_Success() throws Exception {
        BookingRequest request = new BookingRequest();
        request.setScooterId(1L);
        request.setHours(2);
        request.setCardNumber("1234567890123456");

        when(securityUtils.getCurrentUser(any())).thenReturn(testUser);
        when(scooterService.getScooterById(1L)).thenReturn(testScooter);
        when(bookingService.createBooking(any(), any(), anyInt(), anyString(), any())).thenReturn(testBooking);

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetUserBookings_Success() throws Exception {
        when(securityUtils.getCurrentUser(any())).thenReturn(testUser);
        when(bookingService.getUserBookings(1L)).thenReturn(List.of(testBooking));

        mockMvc.perform(get("/api/bookings/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCancelBooking_Success() throws Exception {
        testBooking.setStatus("CANCELLED");
        when(securityUtils.getCurrentUser(any())).thenReturn(testUser);
        when(bookingService.cancelBooking(1L, testUser)).thenReturn(testBooking);

        mockMvc.perform(put("/api/bookings/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testReturnScooterEarly_Success() throws Exception {
        testBooking.setStatus("COMPLETED");
        when(securityUtils.getCurrentUser(any())).thenReturn(testUser);
        when(bookingService.returnScooterEarly(1L, testUser)).thenReturn(testBooking);

        mockMvc.perform(put("/api/bookings/1/return"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testExtendBooking_Success() throws Exception {
        testBooking.setTotalPrice(new BigDecimal("80.00"));
        when(securityUtils.getCurrentUser(any())).thenReturn(testUser);
        when(bookingService.extendBooking(1L, 2, testUser)).thenReturn(testBooking);

        mockMvc.perform(put("/api/bookings/1/extend")
                        .param("hours", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPrice").value(80.00));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetActiveBookingsCount() throws Exception {
        when(bookingService.getActiveBookingsCount()).thenReturn(5);

        mockMvc.perform(get("/api/bookings/active/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }

    @Test
    void testCreateBooking_Unauthorized() throws Exception {
        BookingRequest request = new BookingRequest();
        request.setScooterId(1L);
        request.setHours(2);

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
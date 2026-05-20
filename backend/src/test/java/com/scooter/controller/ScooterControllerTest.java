package com.scooter.controller;

import com.scooter.entity.Scooter;
import com.scooter.service.ScooterService;
import com.scooter.config.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ScooterController.class)
class ScooterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScooterService scooterService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private com.scooter.service.UserService userService;

    @MockBean
    private com.scooter.util.SecurityUtils securityUtils;

    @MockBean
    private com.scooter.service.SecurityAuditService securityAuditService;

    @MockBean
    private com.scooter.service.EmailService emailService;

    private Scooter testScooter;

    @BeforeEach
    void setUp() {
        testScooter = new Scooter();
        testScooter.setId(1L);
        testScooter.setModel("测试滑板车");
        testScooter.setImageUrl("/images/scooter.jpg");
        testScooter.setTotalQuantity(10);
        testScooter.setAvailableQuantity(8);
        testScooter.setHourlyRate(new BigDecimal("20.00"));
        testScooter.setDailyRate(new BigDecimal("100.00"));
        testScooter.setStatus("AVAILABLE");
    }

    @Test
    void testGetAllScooters() throws Exception {
        when(scooterService.getAllScooters()).thenReturn(List.of(testScooter));

        mockMvc.perform(get("/api/scooters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].model").value("测试滑板车"));
    }

    @Test
    void testGetAvailableScooters() throws Exception {
        when(scooterService.getAvailableScooters()).thenReturn(List.of(testScooter));

        mockMvc.perform(get("/api/scooters/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateScooter_AsAdmin() throws Exception {
        when(scooterService.createScooter(anyString(), any(), anyInt(), anyDouble(), anyDouble(), any()))
                .thenReturn(testScooter);

        mockMvc.perform(post("/api/scooters")
                        .param("model", "新滑板车")
                        .param("totalQuantity", "5")
                        .param("hourlyRate", "25.0")
                        .param("dailyRate", "120.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("测试滑板车"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateScooter_AsAdmin() throws Exception {
        when(scooterService.updateScooter(anyLong(), any(), any(), any(), any(), any(), any()))
                .thenReturn(testScooter);

        mockMvc.perform(put("/api/scooters/1")
                        .param("model", "更新滑板车")
                        .param("hourlyRate", "25.0"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllScooters_NoAuth() throws Exception {
        when(scooterService.getAllScooters()).thenReturn(List.of(testScooter));

        mockMvc.perform(get("/api/scooters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].model").value("测试滑板车"));
    }
}
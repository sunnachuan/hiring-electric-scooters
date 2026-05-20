package com.scooter.service;

import com.scooter.entity.Scooter;
import com.scooter.repository.ScooterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScooterServiceTest {

    @Mock
    private ScooterRepository scooterRepository;

    @InjectMocks
    private ScooterService scooterService;

    private Scooter testScooter;

    @BeforeEach
    void setUp() {
        testScooter = new Scooter();
        testScooter.setId(1L);
        testScooter.setModel("测试滑板车");
        testScooter.setImageUrl("http://example.com/scooter.jpg");
        testScooter.setTotalQuantity(10);
        testScooter.setAvailableQuantity(5);
        testScooter.setHourlyRate(new BigDecimal("20.00"));
        testScooter.setDailyRate(new BigDecimal("100.00"));
        testScooter.setStatus("AVAILABLE");
        testScooter.setLocationId(1);
        testScooter.setLocationName("市中心广场");
        testScooter.setLatitude(39.9042);
        testScooter.setLongitude(116.4074);
    }

    @Test
    void testGetAllScooters() {
        when(scooterRepository.findAll()).thenReturn(List.of(testScooter));

        List<Scooter> scooters = scooterService.getAllScooters();

        assertFalse(scooters.isEmpty());
        assertEquals(1, scooters.size());
        verify(scooterRepository, times(1)).findAll();
    }

    @Test
    void testGetAvailableScooters() {
        when(scooterRepository.findAvailableScooters()).thenReturn(List.of(testScooter));

        List<Scooter> scooters = scooterService.getAvailableScooters();

        assertFalse(scooters.isEmpty());
        assertEquals(1, scooters.size());
        verify(scooterRepository, times(1)).findAvailableScooters();
    }

    @Test
    void testCreateScooter_Success() {
        when(scooterRepository.save(any(Scooter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Scooter created = scooterService.createScooter("新滑板车", "http://example.com/new.jpg", 5, 25.0, 120.0, 2);

        assertNotNull(created);
        assertEquals("新滑板车", created.getModel());
        assertEquals(5, created.getTotalQuantity());
        assertEquals(5, created.getAvailableQuantity());
        assertEquals("AVAILABLE", created.getStatus());
        assertEquals("大学城校区", created.getLocationName());
        verify(scooterRepository, times(1)).save(any(Scooter.class));
    }

    @Test
    void testCreateScooter_WithoutLocation() {
        when(scooterRepository.save(any(Scooter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Scooter created = scooterService.createScooter("新滑板车", null, 0, 25.0, 120.0, null);

        assertNotNull(created);
        assertNull(created.getLocationId());
        assertNull(created.getLocationName());
        assertEquals("UNAVAILABLE", created.getStatus());
    }

    @Test
    void testUpdateScooter_Success() {
        when(scooterRepository.findById(1L)).thenReturn(Optional.of(testScooter));
        when(scooterRepository.save(any(Scooter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Scooter updated = scooterService.updateScooter(1L, "更新的滑板车", "newurl.jpg", 15, 30.0, 150.0, 3);

        assertNotNull(updated);
        assertEquals("更新的滑板车", updated.getModel());
        assertEquals(15, updated.getTotalQuantity());
        assertEquals(10, updated.getAvailableQuantity());
        assertEquals("商业步行街", updated.getLocationName());
        verify(scooterRepository, times(1)).findById(1L);
        verify(scooterRepository, times(1)).save(any(Scooter.class));
    }

    @Test
    void testUpdateScooter_NotFound() {
        when(scooterRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            scooterService.updateScooter(999L, null, null, null, null, null, null));
    }

    @Test
    void testDecrementAvailableQuantity_Success() {
        when(scooterRepository.findById(1L)).thenReturn(Optional.of(testScooter));
        when(scooterRepository.save(any(Scooter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        scooterService.decrementAvailableQuantity(1L);

        assertEquals(4, testScooter.getAvailableQuantity());
        verify(scooterRepository, times(1)).save(testScooter);
    }

    @Test
    void testDecrementAvailableQuantity_ZeroAvailable() {
        testScooter.setAvailableQuantity(0);
        when(scooterRepository.findById(1L)).thenReturn(Optional.of(testScooter));

        assertThrows(RuntimeException.class, () -> 
            scooterService.decrementAvailableQuantity(1L));
    }

    @Test
    void testDecrementAvailableQuantity_BecomesZero() {
        testScooter.setAvailableQuantity(1);
        when(scooterRepository.findById(1L)).thenReturn(Optional.of(testScooter));
        when(scooterRepository.save(any(Scooter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        scooterService.decrementAvailableQuantity(1L);

        assertEquals(0, testScooter.getAvailableQuantity());
        assertEquals("UNAVAILABLE", testScooter.getStatus());
    }

    @Test
    void testIncrementAvailableQuantity_Success() {
        when(scooterRepository.findById(1L)).thenReturn(Optional.of(testScooter));
        when(scooterRepository.save(any(Scooter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        scooterService.incrementAvailableQuantity(1L);

        assertEquals(6, testScooter.getAvailableQuantity());
        verify(scooterRepository, times(1)).save(testScooter);
    }

    @Test
    void testIncrementAvailableQuantity_BecomeAvailable() {
        testScooter.setAvailableQuantity(0);
        testScooter.setStatus("UNAVAILABLE");
        when(scooterRepository.findById(1L)).thenReturn(Optional.of(testScooter));
        when(scooterRepository.save(any(Scooter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        scooterService.incrementAvailableQuantity(1L);

        assertEquals(1, testScooter.getAvailableQuantity());
        assertEquals("AVAILABLE", testScooter.getStatus());
    }

    @Test
    void testGetScooterById_Success() {
        when(scooterRepository.findById(1L)).thenReturn(Optional.of(testScooter));

        Scooter found = scooterService.getScooterById(1L);

        assertNotNull(found);
        assertEquals(1L, found.getId());
    }

    @Test
    void testGetScooterById_NotFound() {
        when(scooterRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            scooterService.getScooterById(999L));
    }
}

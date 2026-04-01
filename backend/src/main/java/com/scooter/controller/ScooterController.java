package com.scooter.controller;

import com.scooter.entity.Scooter;
import com.scooter.service.ScooterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scooters")
@RequiredArgsConstructor
public class ScooterController {
    
    private final ScooterService scooterService;
    
    @GetMapping
    public ResponseEntity<List<Scooter>> getAllScooters() {
        return ResponseEntity.ok(scooterService.getAllScooters());
    }
    
    @GetMapping("/available")
    public ResponseEntity<List<Scooter>> getAvailableScooters() {
        return ResponseEntity.ok(scooterService.getAvailableScooters());
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Scooter> createScooter(@RequestParam String model,
                                                @RequestParam(required = false) String imageUrl,
                                                @RequestParam Integer totalQuantity,
                                                @RequestParam Double hourlyRate,
                                                @RequestParam Double dailyRate) {
        return ResponseEntity.ok(scooterService.createScooter(model, imageUrl, totalQuantity, hourlyRate, dailyRate));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Scooter> updateScooter(@PathVariable Long id,
                                                @RequestParam(required = false) String model,
                                                @RequestParam(required = false) String imageUrl,
                                                @RequestParam(required = false) Integer totalQuantity,
                                                @RequestParam(required = false) Double hourlyRate,
                                                @RequestParam(required = false) Double dailyRate) {
        return ResponseEntity.ok(scooterService.updateScooter(id, model, imageUrl, totalQuantity, hourlyRate, dailyRate));
    }
}
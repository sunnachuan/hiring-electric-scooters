package com.scooter.repository;

import com.scooter.entity.Scooter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScooterRepository extends JpaRepository<Scooter, Long> {
    List<Scooter> findByStatus(String status);
    
    @Query("SELECT s FROM Scooter s WHERE s.status = 'AVAILABLE'")
    List<Scooter> findAvailableScooters();
}
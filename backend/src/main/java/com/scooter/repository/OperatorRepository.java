package com.scooter.repository;

import com.scooter.entity.Operator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperatorRepository extends JpaRepository<Operator, Long> {
    
    List<Operator> findByRole(String role);
    
    List<Operator> findByStatus(String status);
    
    @Query("SELECT o FROM Operator o WHERE o.role = :role AND o.status = 'ACTIVE' AND o.currentTaskCount < 3")
    List<Operator> findAvailableOperatorsByRole(@Param("role") String role);
    
    @Query("SELECT o FROM Operator o WHERE o.assignedArea LIKE %:area% AND o.status = 'ACTIVE'")
    List<Operator> findByAssignedAreaContaining(@Param("area") String area);
}
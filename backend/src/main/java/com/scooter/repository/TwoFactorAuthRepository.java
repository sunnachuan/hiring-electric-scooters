package com.scooter.repository;

import com.scooter.entity.TwoFactorAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TwoFactorAuthRepository extends JpaRepository<TwoFactorAuth, Long> {
    
    Optional<TwoFactorAuth> findByUserId(Long userId);
    
    void deleteByUserId(Long userId);
}
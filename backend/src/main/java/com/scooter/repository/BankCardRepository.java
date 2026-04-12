package com.scooter.repository;

import com.scooter.entity.BankCard;
import com.scooter.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankCardRepository extends JpaRepository<BankCard, Long> {
    
    List<BankCard> findByUserIdOrderByIsDefaultDescLastUsedAtDesc(Long userId);
    
    Optional<BankCard> findByUserIdAndIsDefaultTrue(Long userId);
    
    Optional<BankCard> findByUserIdAndCardNumber(Long userId, String cardNumber);
    
    @Query("SELECT bc FROM BankCard bc WHERE bc.user = :user AND bc.isDefault = true")
    Optional<BankCard> findDefaultCardByUser(@Param("user") User user);
    
    @Query("SELECT bc FROM BankCard bc WHERE bc.user.id = :userId AND bc.cardNumber LIKE :lastFourDigits")
    List<BankCard> findByUserIdAndCardNumberEndsWith(@Param("userId") Long userId, 
                                                     @Param("lastFourDigits") String lastFourDigits);
    
    boolean existsByUserIdAndCardNumber(Long userId, String cardNumber);
}
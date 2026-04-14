package com.scooter.repository;

import com.scooter.entity.BankCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 银行卡数据访问接口
 */
@Repository
public interface BankCardRepository extends JpaRepository<BankCard, Long> {
    
    /**
     * 根据用户ID查找银行卡列表
     */
    List<BankCard> findByUserIdAndStatusOrderByIsDefaultDescCreatedAtDesc(Long userId, String status);
    
    /**
     * 根据用户ID查找所有银行卡（包括停用的）
     */
    List<BankCard> findByUserIdOrderByIsDefaultDescCreatedAtDesc(Long userId);
    
    /**
     * 根据用户ID和卡片ID查找银行卡
     */
    Optional<BankCard> findByIdAndUserId(Long id, Long userId);
    
    /**
     * 统计用户的有效银行卡数量
     */
    Long countByUserIdAndStatus(Long userId, String status);
    
    /**
     * 查找用户的默认银行卡
     */
    Optional<BankCard> findByUserIdAndIsDefaultTrueAndStatus(Long userId, String status);
    
    /**
     * 取消其他银行卡的默认设置
     */
    @Modifying
    @Query("UPDATE BankCard b SET b.isDefault = false WHERE b.userId = :userId AND b.id != :excludeId AND b.isDefault = true")
    void unsetOtherDefaultCards(@Param("userId") Long userId, @Param("excludeId") Long excludeId);
    
    /**
     * 设置指定银行卡为默认
     */
    @Modifying
    @Query("UPDATE BankCard b SET b.isDefault = true WHERE b.id = :id AND b.userId = :userId")
    int setAsDefaultCard(@Param("id") Long id, @Param("userId") Long userId);
    
    /**
     * 检查银行卡号是否已存在（同一用户）
     */
    boolean existsByUserIdAndCardNumberAndStatus(Long userId, String cardNumber, String status);
}
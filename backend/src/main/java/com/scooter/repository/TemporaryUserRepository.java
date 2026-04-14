package com.scooter.repository;

import com.scooter.entity.TemporaryUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 临时用户数据访问接口
 */
@Repository
public interface TemporaryUserRepository extends JpaRepository<TemporaryUser, Long> {
    
    /**
     * 根据手机号查找活跃的临时用户
     */
    Optional<TemporaryUser> findByPhoneAndStatus(String phone, String status);
    
    /**
     * 根据创建店员查找临时用户
     */
    List<TemporaryUser> findByCreatedByAndStatusOrderByLastUsedAtDesc(Long createdBy, String status);
    
    /**
     * 查找所有活跃的临时用户
     */
    List<TemporaryUser> findByStatusOrderByLastUsedAtDesc(String status);
    
    /**
     * 检查手机号是否已存在（活跃用户）
     */
    boolean existsByPhoneAndStatus(String phone, String status);
    
    /**
     * 更新最后使用时间
     */
    @Query("UPDATE TemporaryUser t SET t.lastUsedAt = CURRENT_TIMESTAMP WHERE t.id = :id")
    void updateLastUsedTime(@Param("id") Long id);
}
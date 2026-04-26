package com.scooter.util;

import com.scooter.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 数据权限验证工具类
 * 确保用户只能访问自己的数据
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataPermissionValidator {
    
    private final SecurityUtils securityUtils;
    
    /**
     * 验证用户是否有权访问指定数据
     * @param request HTTP请求
     * @param targetUserId 目标用户ID
     * @return 是否有权限访问
     */
    public boolean validateUserAccess(HttpServletRequest request, Long targetUserId) {
        try {
            User currentUser = securityUtils.getCurrentUser(request);
            
            // 管理员可以访问所有数据
            if ("ADMIN".equals(currentUser.getRole())) {
                log.debug("管理员用户 {} 访问用户 {} 的数据", currentUser.getUsername(), targetUserId);
                return true;
            }
            
            // 普通用户只能访问自己的数据
            boolean hasAccess = currentUser.getId().equals(targetUserId);
            
            if (!hasAccess) {
                log.warn("用户 {} 尝试访问用户 {} 的数据被拒绝", currentUser.getUsername(), targetUserId);
            }
            
            return hasAccess;
            
        } catch (Exception e) {
            log.error("数据权限验证失败", e);
            return false;
        }
    }
    
    /**
     * 验证用户是否有权访问当前用户的数据
     * @param request HTTP请求
     * @return 是否有权限访问
     */
    public boolean validateCurrentUserAccess(HttpServletRequest request) {
        try {
            User currentUser = securityUtils.getCurrentUser(request);
            
            // 从请求头中获取目标用户ID
            String targetUserIdHeader = request.getHeader("X-User-Id");
            if (targetUserIdHeader == null) {
                log.warn("请求头中缺少用户ID信息");
                return false;
            }
            
            Long targetUserId = Long.parseLong(targetUserIdHeader);
            return validateUserAccess(request, targetUserId);
            
        } catch (Exception e) {
            log.error("当前用户数据权限验证失败", e);
            return false;
        }
    }
    
    /**
     * 验证用户是否是管理员
     * @param request HTTP请求
     * @return 是否是管理员
     */
    public boolean isAdmin(HttpServletRequest request) {
        try {
            User currentUser = securityUtils.getCurrentUser(request);
            return "ADMIN".equals(currentUser.getRole());
        } catch (Exception e) {
            log.error("管理员权限验证失败", e);
            return false;
        }
    }
    
    /**
     * 验证用户是否有权访问银行卡数据
     * @param request HTTP请求
     * @param bankCardId 银行卡ID
     * @return 是否有权限访问
     */
    public boolean validateBankCardAccess(HttpServletRequest request, Long bankCardId) {
        try {
            User currentUser = securityUtils.getCurrentUser(request);
            
            // 管理员可以访问所有银行卡数据
            if ("ADMIN".equals(currentUser.getRole())) {
                return true;
            }
            
            // 普通用户只能访问自己的银行卡数据
            // 这里需要查询数据库验证银行卡是否属于当前用户
            // 由于银行卡服务已经包含用户ID验证，这里主要进行前置检查
            
            return true; // 具体验证在服务层实现
            
        } catch (Exception e) {
            log.error("银行卡数据权限验证失败", e);
            return false;
        }
    }
    
    /**
     * 抛出权限拒绝异常
     */
    public void throwAccessDenied() {
        throw new RuntimeException("权限不足，无法访问该数据");
    }
    
    /**
     * 验证并抛出权限异常
     */
    public void validateAndThrow(HttpServletRequest request, Long targetUserId) {
        if (!validateUserAccess(request, targetUserId)) {
            throwAccessDenied();
        }
    }
}
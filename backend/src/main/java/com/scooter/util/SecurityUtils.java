package com.scooter.util;

import com.scooter.config.JwtUtils;
import com.scooter.entity.User;
import com.scooter.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Component
@RequiredArgsConstructor
public class SecurityUtils {
    
    private final JwtUtils jwtUtils;
    private final UserService userService;
    
    /**
     * 从请求中获取当前用户
     */
    public User getCurrentUser(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String username = jwtUtils.extractUsername(token);
            return userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
        }
        throw new RuntimeException("未找到认证信息");
    }
    
    /**
     * 从当前请求上下文中获取当前用户
     */
    public User getCurrentUser() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return getCurrentUser(request);
        }
        throw new RuntimeException("无法获取请求上下文");
    }
}
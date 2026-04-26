package com.scooter.service;

import com.scooter.entity.User;
import com.scooter.repository.UserRepository;
import com.scooter.util.PasswordPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicy passwordPolicy;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
        
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .roles(user.getRole())
                .build();
    }
    
    public User createUser(String username, String email, String password, String role, 
                          Boolean isStudent, Boolean isSenior, String phone, String fullName) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("邮箱已存在");
        }
        
        // 验证密码策略
        var passwordValidation = passwordPolicy.validatePassword(password);
        if (!passwordValidation.isValid()) {
            throw new RuntimeException(passwordValidation.getMessage());
        }
        
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        user.setIsStudent(isStudent);
        user.setIsSenior(isSenior);
        user.setPhone(phone);
        user.setFullName(fullName);
        
        return userRepository.save(user);
    }
    
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public java.util.List<User> findAll() {
        return userRepository.findAll();
    }
    
    public User updateUser(User user) {
        return userRepository.save(user);
    }
    
    public boolean changePassword(String username, String currentPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new RuntimeException("当前密码错误");
        }
        
        // 验证新密码策略
        var passwordValidation = passwordPolicy.validatePassword(newPassword);
        if (!passwordValidation.isValid()) {
            throw new RuntimeException(passwordValidation.getMessage());
        }
        
        // 检查新密码是否与旧密码相同
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new RuntimeException("新密码不能与当前密码相同");
        }
        
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        return true;
    }
}
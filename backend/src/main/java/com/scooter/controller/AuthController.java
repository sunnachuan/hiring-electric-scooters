package com.scooter.controller;

import com.scooter.dto.AuthRequest;
import com.scooter.dto.AuthResponse;
import com.scooter.dto.RegisterRequest;
import com.scooter.entity.User;
import com.scooter.service.UserService;
import com.scooter.config.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody AuthRequest authRequest) {
        User user = userService.findByUsername(authRequest.getUsername()).orElse(null);
        
        if (user == null || !passwordEncoder.matches(authRequest.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.badRequest().body("用户名或密码错误");
        }
        
        String jwt = jwtUtils.generateToken(user.getUsername());
        
        return ResponseEntity.ok(new AuthResponse(jwt, user.getId(), user.getUsername(), 
                user.getEmail(), user.getRole()));
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        User user = userService.createUser(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                registerRequest.getPassword(),
                registerRequest.getRole(),
                registerRequest.getIsStudent(),
                registerRequest.getIsSenior()
        );
        
        String jwt = jwtUtils.generateToken(user.getUsername());
        
        return ResponseEntity.ok(new AuthResponse(jwt, user.getId(), user.getUsername(), 
                user.getEmail(), user.getRole()));
    }
}
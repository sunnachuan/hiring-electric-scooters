package com.scooter.service;

import com.scooter.entity.User;
import com.scooter.repository.UserRepository;
import com.scooter.util.PasswordPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordPolicy passwordPolicy;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedPassword");
        testUser.setRole("USER");
        testUser.setIsStudent(false);
        testUser.setIsSenior(false);
    }

    @Test
    void testLoadUserByUsername_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        var userDetails = userService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("hashedPassword", userDetails.getPassword());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.loadUserByUsername("nonexistent"));
    }

    @Test
    void testCreateUser_Success() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordPolicy.validatePassword("StrongPass123!")).thenReturn(new PasswordPolicy.PasswordValidationResult(true, null));
        when(passwordEncoder.encode("StrongPass123!")).thenReturn("hashedNewPass");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User createdUser = userService.createUser("newuser", "newuser@example.com", "StrongPass123!", "USER", false, false, "13800138000", "新用户");

        assertNotNull(createdUser);
        assertEquals("newuser", createdUser.getUsername());
        assertEquals("newuser@example.com", createdUser.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testCreateUser_UsernameContainsChinese() {
        assertThrows(RuntimeException.class, () -> 
            userService.createUser("测试用户", "test@example.com", "StrongPass123!", "USER", false, false, null, null));
    }

    @Test
    void testCreateUser_UsernameInvalidCharacters() {
        assertThrows(RuntimeException.class, () -> 
            userService.createUser("user!name", "test@example.com", "StrongPass123!", "USER", false, false, null, null));
    }

    @Test
    void testCreateUser_UsernameAlreadyExists() {
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> 
            userService.createUser("existinguser", "test@example.com", "StrongPass123!", "USER", false, false, null, null));
    }

    @Test
    void testCreateUser_EmailAlreadyExists() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> 
            userService.createUser("newuser", "existing@example.com", "StrongPass123!", "USER", false, false, null, null));
    }

    @Test
    void testCreateUser_PasswordValidationFailed() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordPolicy.validatePassword("weak")).thenReturn(new PasswordPolicy.PasswordValidationResult(false, "密码强度不够"));

        assertThrows(RuntimeException.class, () -> 
            userService.createUser("newuser", "newuser@example.com", "weak", "USER", false, false, null, null));
    }

    @Test
    void testFindByUsername_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        Optional<User> found = userService.findByUsername("testuser");

        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
    }

    @Test
    void testFindByUsername_NotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        Optional<User> found = userService.findByUsername("nonexistent");

        assertFalse(found.isPresent());
    }

    @Test
    void testFindByEmail_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        Optional<User> found = userService.findByEmail("test@example.com");

        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void testFindAll() {
        when(userRepository.findAll()).thenReturn(List.of(testUser));

        List<User> users = userService.findAll();

        assertFalse(users.isEmpty());
        assertEquals(1, users.size());
    }

    @Test
    void testUpdateUser() {
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User updated = userService.updateUser(testUser);

        assertNotNull(updated);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void testChangePassword_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("currentPass", "hashedPassword")).thenReturn(true);
        when(passwordPolicy.validatePassword("NewPass123!")).thenReturn(new PasswordPolicy.PasswordValidationResult(true, null));
        when(passwordEncoder.matches("NewPass123!", "hashedPassword")).thenReturn(false);
        when(passwordEncoder.encode("NewPass123!")).thenReturn("newHashedPass");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = userService.changePassword("testuser", "currentPass", "NewPass123!");

        assertTrue(result);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testChangePassword_UserNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            userService.changePassword("nonexistent", "currentPass", "newPass"));
    }

    @Test
    void testChangePassword_CurrentPasswordWrong() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPass", "hashedPassword")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> 
            userService.changePassword("testuser", "wrongPass", "newPass"));
    }

    @Test
    void testChangePassword_SameAsCurrent() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("currentPass", "hashedPassword")).thenReturn(true);
        when(passwordPolicy.validatePassword("currentPass")).thenReturn(new PasswordPolicy.PasswordValidationResult(true, null));
        when(passwordEncoder.matches("currentPass", "hashedPassword")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> 
            userService.changePassword("testuser", "currentPass", "currentPass"));
    }

    @Test
    void testUpdateUserProfile_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("newemail@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.updateUserProfile(1L, "新姓名", "newemail@example.com", "13900139000");

        assertNotNull(updated);
        assertEquals("新姓名", updated.getFullName());
        assertEquals("newemail@example.com", updated.getEmail());
        assertEquals("13900139000", updated.getPhone());
    }

    @Test
    void testUpdateUserProfile_UserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            userService.updateUserProfile(999L, null, null, null));
    }

    @Test
    void testUpdateUserProfile_EmailAlreadyUsed() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> 
            userService.updateUserProfile(1L, null, "existing@example.com", null));
    }
}

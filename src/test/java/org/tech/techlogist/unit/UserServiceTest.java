package org.tech.techlogist.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.tech.techlogist.dto.user.UserRegisterRequestDto;
import org.tech.techlogist.dto.user.UserResponseDto;
import org.tech.techlogist.dto.user.UserUpdateRequestDto;
import org.tech.techlogist.entity.User;
import org.tech.techlogist.enums.Role;
import org.tech.techlogist.repository.UserRepository;
import org.tech.techlogist.service.UserService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("ipek");
        user.setEmail("ipek@tech.com");
        user.setPassword("encoded_password");
        user.setRole(Role.CUSTOMER);
    }

    @Test
    void register_SuccessfulScenario() {
        UserRegisterRequestDto dto = new UserRegisterRequestDto();
        dto.setUsername("ipek");
        dto.setEmail("ipek@tech.com");
        dto.setPassword("12345");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        UserResponseDto result = userService.register(dto);
        assertNotNull(result);
        assertEquals("ipek", result.getUsername());
        assertEquals("CUSTOMER", result.getRole());
        verify(passwordEncoder).encode("12345");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_WhenUsernameExists_ShouldThrowException() {
        UserRegisterRequestDto dto = new UserRegisterRequestDto();
        dto.setUsername("ipek");
        when(userRepository.findByUsername("ipek")).thenReturn(Optional.of(user));
        assertThrows(RuntimeException.class, () -> userService.register(dto), "Username already exists");
        verify(userRepository, never()).save(any());
    }

    @Test
    void createByAdmin_WhenRoleIsNull_ShouldThrowException() {
        UserUpdateRequestDto dto = new UserUpdateRequestDto();
        dto.setRole(null);
        assertThrows(RuntimeException.class, () -> userService.createByAdmin(dto));
    }

    @Test
    void createByAdmin_SuccessfulScenario() {
        // Arrange
        UserUpdateRequestDto dto = new UserUpdateRequestDto();
        dto.setUsername("admin_user");
        dto.setEmail("admin@tech.com");
        dto.setPassword("pass");
        dto.setRole(Role.ADMIN);
        user.setRole(Role.ADMIN);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);
        UserResponseDto result = userService.createByAdmin(dto);
        assertEquals("ADMIN", result.getRole());
    }

    @Test
    void findByUsername_WhenExists_ShouldReturnUser() {
        when(userRepository.findByUsername("ipek")).thenReturn(Optional.of(user));
        User result = userService.findByUsername("ipek");
        assertNotNull(result);
        assertEquals("ipek", result.getUsername());
    }

    @Test
    void findByUsername_WhenNotExists_ShouldThrowException() {
        when(userRepository.findByUsername("bilinmeyen")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> userService.findByUsername("bilinmeyen"));
    }

    @Test
    void getAllUsers_ShouldReturnList() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        List<UserResponseDto> result = userService.getAllUsers();
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void deleteUser_ShouldCallRepository() {
        userService.deleteUser(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }
}

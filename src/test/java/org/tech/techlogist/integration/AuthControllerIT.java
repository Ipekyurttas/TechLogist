package org.tech.techlogist.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.tech.techlogist.config.JwtUtil;
import org.tech.techlogist.dto.auth.LoginRequestDto;
import org.tech.techlogist.dto.user.UserRegisterRequestDto;
import org.tech.techlogist.dto.user.UserResponseDto;
import org.tech.techlogist.entity.User;
import org.tech.techlogist.enums.Role;
import org.tech.techlogist.service.UserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void register_SuccessfulScenario() throws Exception {
        UserRegisterRequestDto requestDto = new UserRegisterRequestDto();
        requestDto.setUsername("yeniuser");
        requestDto.setPassword("sifre123");
        requestDto.setEmail("yeni@tech.com");
        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(1L);
        responseDto.setUsername("yeniuser");
        when(userService.register(any(UserRegisterRequestDto.class))).thenReturn(responseDto);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("yeniuser"));
    }

    @Test
    void login_SuccessfulScenario() throws Exception {
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setUsername("ipek");
        loginRequest.setPassword("admin123");

        User mockUser = new User();
        mockUser.setId(99L);
        mockUser.setUsername("ipek");
        mockUser.setRole(Role.ADMIN);
        when(userService.findByUsername("ipek")).thenReturn(mockUser);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("mock-jwt-token");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.userId").value(99));
    }

    @Test
    void login_WithWrongCredentials_ShouldReturnUnauthorized() throws Exception {
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setUsername("hata");
        loginRequest.setPassword("yanlis");
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Hatalı şifre"));
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden());
    }
}
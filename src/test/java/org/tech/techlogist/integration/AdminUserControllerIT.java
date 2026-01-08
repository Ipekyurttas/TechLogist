package org.tech.techlogist.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.tech.techlogist.dto.user.UserResponseDto;
import org.tech.techlogist.dto.user.UserUpdateRequestDto;
import org.tech.techlogist.enums.Role;
import org.tech.techlogist.service.UserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminUserControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUserByAdmin_WhenAdmin_ShouldReturnCreatedUser() throws Exception {
        UserUpdateRequestDto requestDto = new UserUpdateRequestDto();
        requestDto.setUsername("yeni_admin");
        requestDto.setRole(Role.ADMIN);

        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(100L);
        responseDto.setUsername("yeni_admin");

        when(userService.createByAdmin(any(UserUpdateRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("yeni_admin"));

        verify(userService).createByAdmin(any(UserUpdateRequestDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_WhenAdmin_ShouldReturnOk() throws Exception {
        mockMvc.perform(delete("/api/admin/users/1"))
                .andExpect(status().isOk());

        verify(userService).deleteUser(1L);
    }
}
package org.tech.techlogist.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.tech.techlogist.dto.notification.NotificationResponseDto;
import org.tech.techlogist.entity.User;
import org.tech.techlogist.repository.UserRepository;
import org.tech.techlogist.service.NotificationService;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserNotificationControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    @WithMockUser(username = "testuser")
    void myNotifications_ShouldReturnUserNotifications() throws Exception {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        NotificationResponseDto notification = new NotificationResponseDto();
        notification.setId(10L);
        notification.setTitle("Hoş Geldiniz");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(notificationService.getUserNotifications(1L)).thenReturn(List.of(notification));
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Hoş Geldiniz"));
    }

    @Test
    @WithMockUser
    void markAsRead_ShouldReturnOk() throws Exception {
        mockMvc.perform(put("/api/notifications/10/read"))
                .andExpect(status().isOk());

        verify(notificationService).markAsRead(10L);
    }
}
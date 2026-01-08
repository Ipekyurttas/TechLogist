package org.tech.techlogist.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.tech.techlogist.dto.notification.NotificationCreateRequestDto;
import org.tech.techlogist.service.NotificationService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminNotificationControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void sendNotification_WhenUserIsAdmin_ShouldReturnOk() throws Exception {
        NotificationCreateRequestDto dto = new NotificationCreateRequestDto();
        dto.setTitle("Kampanya");
        dto.setMessage("Tüm ürünlerde %20 indirim!");
        dto.setUserId(null);

        mockMvc.perform(post("/api/admin/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(notificationService).sendNotification(any(NotificationCreateRequestDto.class));
    }

    @Test
    void sendNotification_WhenUserNotAuthenticated_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/admin/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }
}
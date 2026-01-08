package org.tech.techlogist.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tech.techlogist.dto.notification.NotificationCreateRequestDto;
import org.tech.techlogist.dto.notification.NotificationResponseDto;
import org.tech.techlogist.entity.Notification;
import org.tech.techlogist.entity.User;
import org.tech.techlogist.repository.NotificationRepository;
import org.tech.techlogist.repository.UserRepository;
import org.tech.techlogist.service.NotificationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User user;
    private Notification notification;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        notification = new Notification();
        notification.setId(10L);
        notification.setTitle("Test Başlığı");
        notification.setMessage("Test Mesajı");
        notification.setUser(user);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void sendNotification_WhenUserIdIsNull_ShouldSendToAllUsers() {
        NotificationCreateRequestDto dto = new NotificationCreateRequestDto();
        dto.setTitle("Genel Duyuru");
        dto.setMessage("Herkese Merhaba");
        dto.setUserId(null);
        when(userRepository.findAll()).thenReturn(List.of(user, new User()));
        notificationService.sendNotification(dto);
        verify(userRepository, times(1)).findAll();
        verify(notificationRepository, times(2)).save(any(Notification.class)); // 2 kullanıcı için 2 kayıt
    }

    @Test
    void sendNotification_WhenUserIdIsPresent_ShouldSendToSpecificUser() {
        // Arrange
        NotificationCreateRequestDto dto = new NotificationCreateRequestDto();
        dto.setUserId(1L);
        dto.setTitle("Özel Mesaj");
        dto.setMessage("Sadece sana");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        notificationService.sendNotification(dto);
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(userRepository, never()).findAll();
    }

    @Test
    void sendNotification_WhenUserNotFound_ShouldThrowException() {
        NotificationCreateRequestDto dto = new NotificationCreateRequestDto();
        dto.setUserId(99L);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> notificationService.sendNotification(dto));
    }

    @Test
    void getUserNotifications_ShouldReturnList() {
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(notification));
        List<NotificationResponseDto> result = notificationService.getUserNotifications(1L);
        assertEquals(1, result.size());
        assertEquals("Test Başlığı", result.get(0).getTitle());
    }

    @Test
    void markAsRead_WhenNotificationExists_ShouldSetReadToTrue() {
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));
        notificationService.markAsRead(10L);
        assertTrue(notification.isRead()); // setRead(true) kontrolü
        verify(notificationRepository).save(notification);
    }

    @Test
    void markAsRead_WhenNotificationNotFound_ShouldThrowException() {
        when(notificationRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> notificationService.markAsRead(10L));
    }
}
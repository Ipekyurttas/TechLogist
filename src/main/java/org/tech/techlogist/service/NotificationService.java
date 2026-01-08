package org.tech.techlogist.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tech.techlogist.dto.notification.NotificationCreateRequestDto;
import org.tech.techlogist.dto.notification.NotificationResponseDto;
import org.tech.techlogist.entity.Notification;
import org.tech.techlogist.entity.User;
import org.tech.techlogist.repository.NotificationRepository;
import org.tech.techlogist.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;


    public void sendNotification(NotificationCreateRequestDto dto) {

        if (dto.getUserId() == null) {
            List<User> users = userRepository.findAll();
            users.forEach(user -> createNotification(dto, user));
        } else {
            User user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            createNotification(dto, user);
        }
    }


    public List<NotificationResponseDto> getUserNotifications(Long userId) {

        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public void markAsRead(Long notificationId) {

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setRead(true);
        notificationRepository.save(notification);
    }


    private void createNotification(
            NotificationCreateRequestDto dto,
            User user) {

        Notification notification = new Notification();
        notification.setTitle(dto.getTitle());
        notification.setMessage(dto.getMessage());
        notification.setUser(user);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(notification);
    }

    private NotificationResponseDto mapToResponse(Notification n) {
        NotificationResponseDto dto = new NotificationResponseDto();
        dto.setId(n.getId());
        dto.setTitle(n.getTitle());
        dto.setMessage(n.getMessage());
        dto.setRead(n.isRead());
        dto.setCreatedAt(n.getCreatedAt());
        return dto;
    }
}

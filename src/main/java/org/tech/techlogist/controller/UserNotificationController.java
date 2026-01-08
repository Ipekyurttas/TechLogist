package org.tech.techlogist.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.tech.techlogist.dto.notification.NotificationResponseDto;
import org.tech.techlogist.service.NotificationService;
import org.tech.techlogist.repository.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class UserNotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping
    public List<NotificationResponseDto> myNotifications(
            Authentication authentication) {

        String username = authentication.getName();

        Long userId = userRepository.findByUsername(username)
                .orElseThrow()
                .getId();

        return notificationService.getUserNotifications(userId);
    }

    @PutMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
    }
}

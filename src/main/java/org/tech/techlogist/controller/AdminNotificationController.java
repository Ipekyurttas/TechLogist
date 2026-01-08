package org.tech.techlogist.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.tech.techlogist.dto.notification.NotificationCreateRequestDto;
import org.tech.techlogist.service.NotificationService;

@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public void sendNotification(
            @RequestBody NotificationCreateRequestDto dto) {

        notificationService.sendNotification(dto);
    }
}

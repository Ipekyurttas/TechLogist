package org.tech.techlogist.dto.notification;

import lombok.Data;

@Data
public class NotificationCreateRequestDto {

    private String title;
    private String message;

    // null ise → tüm kullanıcılara gönder
    private Long userId;
}


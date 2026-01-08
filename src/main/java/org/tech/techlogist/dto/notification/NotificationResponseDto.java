package org.tech.techlogist.dto.notification;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationResponseDto {

    private Long id;
    private String title;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;
}

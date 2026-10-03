package com.eplaceapi.eplace_backend;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        Long userId,
        String title,
        String message,
        NotificationType type,
        boolean isRead,
        LocalDateTime createdAt
) {}
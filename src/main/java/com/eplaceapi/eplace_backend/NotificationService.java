package com.eplaceapi.eplace_backend;

import java.math.BigDecimal;
import java.util.List;

public interface NotificationService {
    void sendApprovalNotification(Long userId, String username, String leagueName, BigDecimal amount, boolean isCreation);
    void sendRejectionNotification(Long userId, String username, String leagueName, BigDecimal amount, boolean isCreation);
    List<NotificationResponse> getUserNotifications(Long userId);
    void markAsRead(Long notificationId);
    void sendNotificationToUser(Long userId, String title, String message);
    long getUnreadCount(Long userId);
}
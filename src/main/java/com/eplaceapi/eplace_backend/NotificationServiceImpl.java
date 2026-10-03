package com.eplaceapi.eplace_backend;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional
    public void sendNotificationToUser(Long userId, String title, String message) {
        Notification notification = new Notification(userId, title, message, NotificationType.APPROVAL);
        notificationRepository.save(notification);

         sendFcmPushToUserTopic(userId, title, message, "SOCIAL");
    }

    @Override
    @Transactional
    public void sendApprovalNotification(Long userId, String username, String leagueName, BigDecimal amount, boolean isCreation) {
        String actionType = isCreation ? "created" : "joined";
        String title = isCreation ? "🎉 League Creation Approved!" : "🎉 League Entry Verified!";

        String message = String.format(
                "Congratulations %s! Your M-Pesa payment of KES %s for %s the league '%s' has been verified by Admin. Get your squad ready!",
                username, amount, actionType, leagueName
        );

         Notification notification = new Notification(userId, title, message, NotificationType.APPROVAL);
        notificationRepository.save(notification);

         sendFcmPushToUserTopic(userId, title, message, "APPROVAL");
    }

    @Override
    @Transactional
    public void sendRejectionNotification(Long userId, String username, String leagueName, BigDecimal amount, boolean isCreation) {
        String actionType = isCreation ? "creation" : "entry";
        String title = "⚠️ Request Unsuccessful";

        String message = String.format(
                "Hello %s, we could not verify your M-Pesa transaction of KES %s for the '%s' request for league '%s'. If you believe this is an error, please contact ePlace support.",
                username, amount, actionType, leagueName
        );

         Notification notification = new Notification(userId, title, message, NotificationType.REJECTION);
        notificationRepository.save(notification);

        sendFcmPushToUserTopic(userId, title, message, "REJECTION");
    }

    private void sendFcmPushToUserTopic(Long userId, String title, String body, String type) {
        try {
            String userTopic = "user_" + userId;

            com.google.firebase.messaging.Notification fcmNotification =
                    com.google.firebase.messaging.Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build();

            Message fcmMessage = Message.builder()
                    .setTopic(userTopic)
                    .setNotification(fcmNotification)
                    .putData("type", type)
                    .putData("userId", String.valueOf(userId))
                    .build();

            String response = FirebaseMessaging.getInstance().send(fcmMessage);
            System.out.println("✅ FCM Push Notification sent successfully to topic [" + userTopic + "]: " + response);

        } catch (Exception e) {
            System.err.println("❌ Failed to send FCM Push Notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with ID: " + notificationId));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    private NotificationResponse mapToResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getUserId(),
                n.getTitle(),
                n.getMessage(),
                n.getType(),
                n.isRead(),
                n.getCreatedAt()
        );
    }
}
package com.eplaceapi.eplace_backend;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final UserProfileRepository userProfileRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   UserProfileRepository userProfileRepository) {
        this.notificationRepository = notificationRepository;
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    @Transactional
    public void sendNotificationToUser(Long userId, String title, String message) {
        Notification notification = new Notification(userId, title, message, NotificationType.SYSTEM);
        notificationRepository.save(notification);

        sendFcmPushToUserTopic(userId, title, message, NotificationType.SYSTEM.name(), null);
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

        Notification notification = new Notification(userId, title, message, NotificationType.LEAGUE);
        notificationRepository.save(notification);

        sendFcmPushToUserTopic(userId, title, message, NotificationType.LEAGUE.name(), null);
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

        Notification notification = new Notification(userId, title, message, NotificationType.LEAGUE);
        notificationRepository.save(notification);

        sendFcmPushToUserTopic(userId, title, message, NotificationType.LEAGUE.name(), null);
    }

    @Override
    @Transactional
    public void sendFriendRequestNotification(Long userId, Long senderId, String title, String message) {
        Notification notification = new Notification(userId, title, message, NotificationType.FRIEND_REQUEST);
        notificationRepository.save(notification);

        UserProfile senderProfile = (senderId != null) ? userProfileRepository.findByUserId(senderId).orElse(null) : null;
        String pictureUrl = senderProfile != null ? senderProfile.getProfilePictureUrl() : null;

        sendFcmPushToUserTopic(userId, title, message, NotificationType.FRIEND_REQUEST.name(), pictureUrl);
    }

    private void sendFcmPushToUserTopic(Long userId, String title, String body, String type, String profilePictureUrl) {
        try {
            String userTopic = "user_" + userId;

            com.google.firebase.messaging.Notification fcmNotification =
                    com.google.firebase.messaging.Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build();

            Message.Builder messageBuilder = Message.builder()
                    .setTopic(userTopic)
                    .setNotification(fcmNotification)
                    .putData("type", type)
                    .putData("userId", String.valueOf(userId));

            if (profilePictureUrl != null && !profilePictureUrl.isBlank()) {
                messageBuilder.putData("senderProfilePictureUrl", profilePictureUrl);
            }

            String response = FirebaseMessaging.getInstance().send(messageBuilder.build());
            log.info("✅ FCM Push Notification sent successfully to topic [{}]: {}", userTopic, response);

        } catch (Exception e) {
            log.error("❌ Failed to send FCM Push Notification: {}", e.getMessage(), e);
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
        String pictureUrl = null;

        if (n.getSenderId() != null) {
            UserProfile senderProfile = userProfileRepository.findByUserId(n.getSenderId()).orElse(null);
            pictureUrl = senderProfile != null ? senderProfile.getProfilePictureUrl() : null;
        }

        return new NotificationResponse(
                n.getId(),
                n.getUserId(),
                n.getTitle(),
                n.getMessage(),
                n.getType(),
                n.isRead(),
                pictureUrl,
                n.getCreatedAt()
        );
    }
}
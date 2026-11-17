package com.eplaceapi.eplace_backend;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class WebSocketChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ConversationParticipantRepository participantRepository;


    @MessageMapping("/chat.send")
    public void sendChatMessage(@Payload SendMessageRequest request, SimpMessageHeaderAccessor headerAccessor) {

        Long senderId = request.getSenderId() != null
                ? request.getSenderId()
                : extractUserId(headerAccessor);

        MessageResponse savedMessage = chatService.sendMessage(senderId, request);

        Long recipientId = request.getRecipientId();

        if (recipientId == null && request.getConversationId() != null) {
            List<ConversationParticipant> participants = participantRepository.findByConversationId(request.getConversationId());
            recipientId = participants.stream()
                    .map(ConversationParticipant::getUserId)
                    .filter(id -> !id.equals(senderId)) // senderId is now effectively final!
                    .findFirst()
                    .orElse(null);
        }


        if (recipientId != null) {
            messagingTemplate.convertAndSendToUser(
                    recipientId.toString(),
                    "/queue/messages",
                    savedMessage
            );
        }

        messagingTemplate.convertAndSendToUser(
                senderId.toString(),
                "/queue/messages",
                savedMessage
        );
    }
    private Long extractUserId(SimpMessageHeaderAccessor headerAccessor) {
        Principal principal = headerAccessor.getUser();
        if (principal != null && principal.getName() != null) {
            try {
                return Long.parseLong(principal.getName());
            } catch (NumberFormatException ignored) {}
        }

        String userIdHeader = headerAccessor.getFirstNativeHeader("user-id");
        if (userIdHeader != null) {
            try {
                return Long.parseLong(userIdHeader);
            } catch (NumberFormatException ignored) {}
        }

        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        if (sessionAttributes != null && sessionAttributes.containsKey("userId")) {
            try {
                return Long.parseLong(sessionAttributes.get("userId").toString());
            } catch (NumberFormatException ignored) {}
        }

        throw new IllegalStateException("WebSocket client is not authenticated: Missing 'user-id' in headers or session.");
    }
}
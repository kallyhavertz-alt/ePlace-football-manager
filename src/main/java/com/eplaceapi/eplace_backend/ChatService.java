package com.eplaceapi.eplace_backend;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ChatService {
    MessageResponse sendMessage(Long senderId, SendMessageRequest request);
    List<ConversationResponse> getUserConversations(Long userId);
    List<MessageResponse> getConversationMessages(Long userId, UUID conversationId, LocalDateTime cursor, int limit);
    void markConversationAsRead(Long userId, UUID conversationId);
}

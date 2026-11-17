package com.eplaceapi.eplace_backend;


import com.eplaceapi.eplace_backend.MessageStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ConversationResponse {

    private UUID conversationId;
    private UserSummaryDto otherParticipant;
    private String lastMessageSnippet;
    private MessageStatus lastMessageStatus;
    private LocalDateTime lastMessageAt;
    private long unreadCount;
    private boolean isNonFriendChat;
    private int messagesSentByMe;
}

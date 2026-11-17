package com.eplaceapi.eplace_backend;


import com.eplaceapi.eplace_backend.MessageStatus;
import com.eplaceapi.eplace_backend.MessageType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MessageResponse {

    private UUID id;
    private UUID conversationId;
    private Long senderId;
    private String content;
    private MessageType type;
    private MessageStatus status;
    private LocalDateTime createdAt;
}

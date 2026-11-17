package com.eplaceapi.eplace_backend;


import com.eplaceapi.eplace_backend.MessageType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class SendMessageRequest {

    private UUID conversationId;

    private Long recipientId;

    private Long senderId;

    @NotBlank(message = "Message content cannot be blank")
    private String content;

    private MessageType type = MessageType.TEXT;
}

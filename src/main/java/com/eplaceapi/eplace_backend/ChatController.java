package com.eplaceapi.eplace_backend;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;


    @PostMapping("/send")
    public ResponseEntity<MessageResponse> sendMessage(
            @RequestParam Long userId,
            @RequestBody SendMessageRequest request) {
        MessageResponse response = chatService.sendMessage(userId, request);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationResponse>> getUserConversations(
            @RequestParam Long userId) {
        List<ConversationResponse> conversations = chatService.getUserConversations(userId);
        return ResponseEntity.ok(conversations);
    }


    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<MessageResponse>> getConversationMessages(
            @PathVariable UUID conversationId,
            @RequestParam Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursor,
            @RequestParam(defaultValue = "30") int limit) {
        List<MessageResponse> messages = chatService.getConversationMessages(userId, conversationId, cursor, limit);
        return ResponseEntity.ok(messages);
    }


    @PutMapping("/conversations/{conversationId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable UUID conversationId,
            @RequestParam Long userId) {
        chatService.markConversationAsRead(userId, conversationId);
        return ResponseEntity.ok().build();
    }
}

package com.eplaceapi.eplace_backend;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    @Transactional
    public MessageResponse sendMessage(Long senderId, SendMessageRequest request) {
        Long recipientId = request.getRecipientId();
        UUID conversationId = request.getConversationId();

        if (senderId.equals(recipientId)) {
            throw new IllegalArgumentException("You cannot message yourself.");
        }


        Conversation conversation;
        if (conversationId != null) {
            conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new RuntimeException("Conversation not found"));
        } else {
            if (recipientId == null) {
                throw new IllegalArgumentException("Recipient ID is required when starting a new chat.");
            }

            conversation = conversationRepository.findDirectConversationBetweenUsers(
                    senderId, recipientId, ConversationType.DIRECT
            ).orElseGet(() -> {
                Conversation newConv = new Conversation();
                newConv.setType(ConversationType.DIRECT);
                Conversation savedConv = conversationRepository.save(newConv);


                ConversationParticipant p1 = new ConversationParticipant();
                p1.setConversation(savedConv);
                p1.setUserId(senderId);
                participantRepository.save(p1);

                ConversationParticipant p2 = new ConversationParticipant();
                p2.setConversation(savedConv);
                p2.setUserId(recipientId);
                participantRepository.save(p2);

                return savedConv;
            });
        }


        boolean areFriends = isFriend(senderId, recipientId);
        ConversationParticipant senderParticipant = participantRepository
                .findByConversationIdAndUserId(conversation.getId(), senderId)
                .orElseThrow(() -> new RuntimeException("Participant record not found"));

        if (!areFriends) {
            if (senderParticipant.getMessagesSentCount() >= 50) {
                throw new IllegalStateException("You have reached the 50-message limit for non-friends. Add this user as a friend to continue chatting.");
            }
            senderParticipant.incrementMessagesSent();
            participantRepository.save(senderParticipant);
        }


        Message message = new Message();
        message.setConversation(conversation);
        message.setSenderId(senderId);
        message.setContent(request.getContent());
        message.setType(request.getType() != null ? request.getType() : MessageType.TEXT);
        message.setStatus(MessageStatus.SENT);

        Message savedMessage = messageRepository.save(message);


        return mapToMessageResponse(savedMessage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponse> getUserConversations(Long userId) {
        List<ConversationParticipant> myParticipations = participantRepository.findUserConversationsOrderedByRecent(userId);

        return myParticipations.stream().map(myPart -> {
            Conversation conv = myPart.getConversation();

            // Find the other participant in this 1v1 chat
            List<ConversationParticipant> allParts = participantRepository.findByConversationId(conv.getId());
            Long otherUserId = allParts.stream()
                    .map(ConversationParticipant::getUserId)
                    .filter(id -> !id.equals(userId))
                    .findFirst()
                    .orElse(userId);

            User otherUser = userRepository.findById(otherUserId).orElse(null);
            UserProfile profile = otherUser != null ? userProfileRepository.findByUserId(otherUser.getId()).orElse(null) : null;

            UserSummaryDto otherDto = new UserSummaryDto();
            if (otherUser != null) {
                otherDto.setId(otherUser.getId());
                otherDto.setUsername(otherUser.getUsername());
                otherDto.setOnline(true); // Connected to presence system later
                otherDto.setLastSeenText("Active now");
            }


            Message lastMsg = messageRepository.findFirstByConversationIdOrderByCreatedAtDesc(conv.getId()).orElse(null);

            long unreadCount = messageRepository.countUnreadMessages(conv.getId(), userId, MessageStatus.READ);
            boolean areFriends = otherUser != null && isFriend(userId, otherUser.getId());

            ConversationResponse resp = new ConversationResponse();
            resp.setConversationId(conv.getId());
            resp.setOtherParticipant(otherDto);
            resp.setLastMessageSnippet(lastMsg != null ? lastMsg.getContent() : "No messages yet");
            resp.setLastMessageStatus(lastMsg != null ? lastMsg.getStatus() : MessageStatus.SENT);
            resp.setLastMessageAt(lastMsg != null ? lastMsg.getCreatedAt() : conv.getCreatedAt());
            resp.setUnreadCount(unreadCount);
            resp.setNonFriendChat(!areFriends);
            resp.setMessagesSentByMe(myPart.getMessagesSentCount());

            return resp;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> getConversationMessages(Long userId, UUID conversationId, LocalDateTime cursor, int limit) {
        PageRequest pageable = PageRequest.of(0, limit);
        List<Message> messages;

        if (cursor == null) {
            messages = messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable);
        } else {
            messages = messageRepository.findOlderMessages(conversationId, cursor, pageable);
        }

        return messages.stream().map(this::mapToMessageResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markConversationAsRead(Long userId, UUID conversationId) {
        messageRepository.markMessagesAsRead(conversationId, userId, MessageStatus.READ);
    }

    private boolean isFriend(Long user1Id, Long user2Id) {
        return friendshipRepository.findFriendshipBetween(user1Id, user2Id)
                .map(f -> f.getStatus() == FriendshipStatus.ACCEPTED)
                .orElse(false);
    }

    private MessageResponse mapToMessageResponse(Message message) {
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setConversationId(message.getConversation().getId());
        response.setSenderId(message.getSenderId());
        response.setContent(message.getContent());
        response.setType(message.getType());
        response.setStatus(message.getStatus());
        response.setCreatedAt(message.getCreatedAt());
        return response;
    }
}

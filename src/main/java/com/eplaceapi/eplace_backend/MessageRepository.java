package com.eplaceapi.eplace_backend;

import com.eplaceapi.eplace_backend.Message;
import com.eplaceapi.eplace_backend.MessageStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    Optional<Message> findFirstByConversationIdOrderByCreatedAtDesc(UUID conversationId);

    List<Message> findByConversationIdOrderByCreatedAtDesc(UUID conversationId, Pageable pageable);

    @Query("""
        SELECT m FROM Message m
        WHERE m.conversation.id = :conversationId
          AND m.createdAt < :cursorTimestamp
        ORDER BY m.createdAt DESC
    """)
    List<Message> findOlderMessages(
            @Param("conversationId") UUID conversationId,
            @Param("cursorTimestamp") LocalDateTime cursorTimestamp,
            Pageable pageable
    );


    @Query("""
        SELECT COUNT(m) FROM Message m
        WHERE m.conversation.id = :conversationId
          AND m.senderId <> :currentUserId
          AND m.status <> :readStatus
    """)
    long countUnreadMessages(
            @Param("conversationId") UUID conversationId,
            @Param("currentUserId") Long currentUserId,
            @Param("readStatus") MessageStatus readStatus
    );

    @Modifying
    @Query("""
        UPDATE Message m
        SET m.status = :readStatus
        WHERE m.conversation.id = :conversationId
          AND m.senderId <> :readerUserId
          AND m.status <> :readStatus
    """)
    int markMessagesAsRead(
            @Param("conversationId") UUID conversationId,
            @Param("readerUserId") Long readerUserId,
            @Param("readStatus") MessageStatus readStatus
    );
}

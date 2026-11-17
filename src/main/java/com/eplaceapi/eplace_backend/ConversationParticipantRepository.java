package com.eplaceapi.eplace_backend;

import com.eplaceapi.eplace_backend.ConversationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, Long> {

    Optional<ConversationParticipant> findByConversationIdAndUserId(UUID conversationId, Long userId);

    List<ConversationParticipant> findByConversationId(UUID conversationId);

     @Query("""
        SELECT cp FROM ConversationParticipant cp
        WHERE cp.userId = :userId
        ORDER BY (
            SELECT COALESCE(MAX(m.createdAt), cp.joinedAt)
            FROM Message m
            WHERE m.conversation = cp.conversation
        ) DESC
    """)
    List<ConversationParticipant> findUserConversationsOrderedByRecent(@Param("userId") Long userId);
}
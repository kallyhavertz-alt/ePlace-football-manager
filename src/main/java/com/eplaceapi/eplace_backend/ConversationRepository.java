package com.eplaceapi.eplace_backend;

import com.eplaceapi.eplace_backend.Conversation;
import com.eplaceapi.eplace_backend.ConversationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    @Query("""
        SELECT c FROM Conversation c
        JOIN ConversationParticipant p1 ON p1.conversation = c
        JOIN ConversationParticipant p2 ON p2.conversation = c
        WHERE c.type = :type
          AND p1.userId = :user1Id
          AND p2.userId = :user2Id
    """)
    Optional<Conversation> findDirectConversationBetweenUsers(
            @Param("user1Id") Long user1Id,
            @Param("user2Id") Long user2Id,
            @Param("type") ConversationType type
    );
}

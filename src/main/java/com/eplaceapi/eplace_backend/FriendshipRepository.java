package com.eplaceapi.eplace_backend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    @Query("SELECT f FROM Friendship f WHERE (f.requester.id = :user1 AND f.addressee.id = :user2) OR (f.requester.id = :user2 AND f.addressee.id = :user1)")
    Optional<Friendship> findFriendshipBetween(@Param("user1") Long user1, @Param("user2") Long user2);

    @Query("SELECT f FROM Friendship f WHERE (f.requester.id = :userId OR f.addressee.id = :userId) AND f.status = 'ACCEPTED'")
    List<Friendship> findAllAcceptedFriends(@Param("userId") Long userId);

    List<Friendship> findByRequesterIdAndStatus(Long requesterId, FriendshipStatus status);

    List<Friendship> findByAddresseeIdAndStatus(Long addresseeId, FriendshipStatus status);

    @Query("SELECT COUNT(f) FROM Friendship f WHERE (f.requester.id = :userId OR f.addressee.id = :userId) AND f.status = 'ACCEPTED'")
    Integer countAcceptedFriends(@Param("userId") Long userId);
}
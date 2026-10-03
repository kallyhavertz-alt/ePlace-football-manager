package com.eplaceapi.eplace_backend;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendshipServiceImpl implements FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService; // FCM notification dispatcher

    @Override
    @Transactional
    public void sendFriendRequest(Long requesterId, Long addresseeId) {
        if (requesterId.equals(addresseeId)) {
            throw new IllegalArgumentException("You cannot send a friend request to yourself.");
        }

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Requester user not found"));
        User addressee = userRepository.findById(addresseeId)
                .orElseThrow(() -> new RuntimeException("Addressee user not found"));

        Optional<Friendship> existing = friendshipRepository.findFriendshipBetween(requesterId, addresseeId);
        if (existing.isPresent()) {
            throw new IllegalStateException("A friendship relationship or request already exists.");
        }

        Friendship friendship = Friendship.builder()
                .requester(requester)
                .addressee(addressee)
                .status(FriendshipStatus.PENDING)
                .build();

        friendshipRepository.save(friendship);

        notificationService.sendNotificationToUser(
                addresseeId,
                "New Friend Request ⚽",
                requester.getUsername() + " sent you a friend request!"
        );
    }

    @Override
    @Transactional
    public void cancelFriendRequest(Long requesterId, Long addresseeId) {
        Friendship friendship = friendshipRepository.findFriendshipBetween(requesterId, addresseeId)
                .orElseThrow(() -> new RuntimeException("Pending friend request not found."));

        if (!friendship.getRequester().getId().equals(requesterId)) {
            throw new SecurityException("Only the requester can cancel this sent request.");
        }

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new IllegalStateException("Cannot cancel a request that is no longer pending.");
        }

        friendshipRepository.delete(friendship);
    }

    @Override
    @Transactional
    public void respondToFriendRequest(Long userId, Long friendshipId, boolean accept) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("Friendship request not found"));

        if (!friendship.getAddressee().getId().equals(userId)) {
            throw new SecurityException("Unauthorized action on friend request.");
        }

        if (accept) {
            friendship.setStatus(FriendshipStatus.ACCEPTED);
            friendshipRepository.save(friendship);

            // Notify Requester that their request was accepted
            notificationService.sendNotificationToUser(
                    friendship.getRequester().getId(),
                    "Friend Request Accepted! 🎉",
                    friendship.getAddressee().getUsername() + " accepted your friend request."
            );
        } else {
            friendship.setStatus(FriendshipStatus.DECLINED);
            friendshipRepository.delete(friendship);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public FriendshipOverviewDTO getFriendshipOverview(Long userId) {
        // 1. Accepted Friends
        List<FriendDTO> friends = friendshipRepository.findAllAcceptedFriends(userId).stream()
                .map(f -> {
                    User other = f.getRequester().getId().equals(userId) ? f.getAddressee() : f.getRequester();
                    return FriendDTO.builder()
                            .id(f.getId())
                            .userId(other.getId())
                            .username(other.getUsername())
                            .email(other.getEmail())
                            .isOnline(true)
                            .build();
                }).collect(Collectors.toList());

        // 2. Pending Outgoing Requests
        List<FriendDTO> pendingSent = friendshipRepository.findByRequesterIdAndStatus(userId, FriendshipStatus.PENDING)
                .stream()
                .map(f -> FriendDTO.builder()
                        .id(f.getId())
                        .userId(f.getAddressee().getId())
                        .username(f.getAddressee().getUsername())
                        .email(f.getAddressee().getEmail())
                        .timeAgo("Recently")
                        .build())
                .collect(Collectors.toList());

        // 3. Received Incoming Requests
        List<FriendDTO> received = friendshipRepository.findByAddresseeIdAndStatus(userId, FriendshipStatus.PENDING)
                .stream()
                .map(f -> FriendDTO.builder()
                        .id(f.getId())
                        .userId(f.getRequester().getId())
                        .username(f.getRequester().getUsername())
                        .email(f.getRequester().getEmail())
                        .timeAgo("Recently")
                        .build())
                .collect(Collectors.toList());

        // 4. Friend Suggestions (All users except existing friends/pending)
        List<FriendDTO> suggestions = userRepository.findAll().stream()
                .filter(u -> !u.getId().equals(userId))
                .filter(u -> friendshipRepository.findFriendshipBetween(userId, u.getId()).isEmpty())
                .limit(5)
                .map(u -> FriendDTO.builder()
                        .userId(u.getId())
                        .username(u.getUsername())
                        .email(u.getEmail())
                        .build())
                .collect(Collectors.toList());

        return FriendshipOverviewDTO.builder()
                .friends(friends)
                .pendingSent(pendingSent)
                .receivedRequests(received)
                .suggestions(suggestions)
                .build();
    }

    @Override
    public void notifyFriendsOnLeagueEvent(Long userId, String leagueTitle, boolean isCreated) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        List<Friendship> friends = friendshipRepository.findAllAcceptedFriends(userId);
        String action = isCreated ? "created a new league" : "joined the league";
        String title = isCreated ? "Friend League Alert 🏆" : "League Joined ⚽";

        for (Friendship f : friends) {
            Long friendId = f.getRequester().getId().equals(userId) ? f.getAddressee().getId() : f.getRequester().getId();
            notificationService.sendNotificationToUser(
                    friendId,
                    title,
                    user.getUsername() + " just " + action + " '" + leagueTitle + "'. Check it out!"
            );
        }
    }
}

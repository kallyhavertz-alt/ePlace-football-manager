package com.eplaceapi.eplace_backend;

public interface FriendshipService {
    void sendFriendRequest(Long requesterId, Long addresseeId);
    void respondToFriendRequest(Long userId, Long friendshipId, boolean accept);
    FriendshipOverviewDTO getFriendshipOverview(Long userId);
    void cancelFriendRequest(Long requesterId, Long addresseeId);
    void notifyFriendsOnLeagueEvent(Long userId, String leagueTitle, boolean isCreated);
}

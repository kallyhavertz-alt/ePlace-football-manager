package com.eplaceapi.eplace_backend;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/friends")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;

    @GetMapping("/{userId}")
    public ResponseEntity<FriendshipOverviewDTO> getFriendshipOverview(@PathVariable Long userId) {
        return ResponseEntity.ok(friendshipService.getFriendshipOverview(userId));
    }

    @PostMapping("/request")
    public ResponseEntity<String> sendFriendRequest(
            @RequestParam Long requesterId,
            @RequestParam Long addresseeId) {
        friendshipService.sendFriendRequest(requesterId, addresseeId);
        return ResponseEntity.ok("Friend request sent successfully.");
    }

    @PutMapping("/respond")
    public ResponseEntity<String> respondToRequest(
            @RequestParam Long userId,
            @RequestParam Long friendshipId,
            @RequestParam boolean accept) {
        friendshipService.respondToFriendRequest(userId, friendshipId, accept);
        return ResponseEntity.ok(accept ? "Request accepted." : "Request declined.");
    }

    @DeleteMapping("/cancel")
    public ResponseEntity<String> cancelFriendRequest(
            @RequestParam Long requesterId,
            @RequestParam Long addresseeId) {
        friendshipService.cancelFriendRequest(requesterId, addresseeId);
        return ResponseEntity.ok("Friend request cancelled successfully.");
    }
}

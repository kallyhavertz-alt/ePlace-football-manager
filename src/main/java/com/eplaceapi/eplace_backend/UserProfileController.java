package com.eplaceapi.eplace_backend;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponseDTO> getProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(userProfileService.getUserProfile(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserProfileResponseDTO>> searchUsers(@RequestParam String query) {
        return ResponseEntity.ok(userProfileService.searchUsersByUsername(query));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserProfileResponseDTO> updateProfile(
            @PathVariable Long userId,
            @RequestBody UpdateProfileRequestDTO request) {
        return ResponseEntity.ok(userProfileService.updateProfile(userId, request));
    }
}
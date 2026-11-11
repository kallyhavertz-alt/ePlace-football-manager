package com.eplaceapi.eplace_backend;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PutMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserProfileResponseDTO> updateProfile(
            @PathVariable Long userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String currentPassword,
            @RequestParam(required = false) String newPassword,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer efootballStrength,
            @RequestParam(required = false) MultipartFile profilePicture) {

        UpdateProfileRequestDTO request = new UpdateProfileRequestDTO();
        request.setUsername(username);
        request.setCurrentPassword(currentPassword);
        request.setNewPassword(newPassword);
        request.setBio(bio);
        request.setLocation(location);
        request.setEfootballStrength(efootballStrength);

        return ResponseEntity.ok(userProfileService.updateProfile(userId, request, profilePicture));
    }
}

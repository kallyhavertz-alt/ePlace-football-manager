package com.eplaceapi.eplace_backend;

import java.util.List;

public interface UserProfileService {
    UserProfileResponseDTO getUserProfile(Long userId);
    UserProfileResponseDTO updateProfile(Long userId, UpdateProfileRequestDTO request);
    List<UserProfileResponseDTO> searchUsersByUsername(String username);
}
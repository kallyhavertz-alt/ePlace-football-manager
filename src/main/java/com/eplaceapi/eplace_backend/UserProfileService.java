package com.eplaceapi.eplace_backend;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserProfileService {
    UserProfileResponseDTO getUserProfile(Long userId);

     UserProfileResponseDTO updateProfile(Long userId, UpdateProfileRequestDTO request);

    @Transactional
    UserProfileResponseDTO updateProfile(Long userId, UpdateProfileRequestDTO request, MultipartFile profilePicture);

    List<UserProfileResponseDTO> searchUsersByUsername(String username);
}
package com.eplaceapi.eplace_backend;

import com.eplaceapi.eplace_backend.UserRegistrationRequest;
import com.eplaceapi.eplace_backend.UserResponse;

public interface UserService {
    UserResponse registerUser(UserRegistrationRequest request);
    UserResponse completeGoogleUserDetails(Long userId, CompleteGoogleRegistrationRequest request);
    UserResponse getUserById(Long id);
    UserResponse getUserByUsername(String username);
}
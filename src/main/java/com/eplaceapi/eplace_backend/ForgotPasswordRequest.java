package com.eplaceapi.eplace_backend;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ForgotPasswordRequest(@NotBlank @Email String email) {}

 record ResetPasswordRequest(
        @NotBlank String token,
        @NotBlank @Size(min = 6, message = "Password must be at least 6 characters") String newPassword
) {}

 record AuthResponse(
        Long userId,
        String username,
        String email,
        String jwtToken
) {}
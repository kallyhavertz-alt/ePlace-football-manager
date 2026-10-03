package com.eplaceapi.eplace_backend;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {}

// For Google, Flutter handles the client authentication and sends us the secure idToken from Google
 record GoogleLoginRequest(
        @NotBlank String idToken
) {}
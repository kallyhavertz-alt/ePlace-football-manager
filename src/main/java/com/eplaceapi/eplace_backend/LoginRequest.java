package com.eplaceapi.eplace_backend;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
         String email,
        @NotBlank String password,
         String phoneNumber
) {}


 record GoogleLoginRequest(
        @NotBlank String idToken
) {}
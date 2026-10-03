package com.eplaceapi.eplace_backend;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record UserRegistrationRequest(
        @NotBlank(message = "PES username cannot be blank")
        @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Please provide a valid email address")
        String email,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Please provide a valid phone number")
        String phoneNumber,

        @NotBlank(message = "6-character password required")
        String password,

        @NotNull(message = "Date of birth is required")
        LocalDate dateOfBirth
) {}
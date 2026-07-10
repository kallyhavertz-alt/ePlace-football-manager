package com.eplaceapi.eplace_backend;


import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String username,
        String email,
        String phoneNumber,
        LocalDate dateOfBirth,
        LocalDateTime createdAt
) {}
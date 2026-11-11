package com.eplaceapi.eplace_backend;

import java.time.LocalDate;

public record CompleteGoogleRegistrationRequest(
        String phoneNumber,
        LocalDate dateOfBirth,
        String newPassword
) {}

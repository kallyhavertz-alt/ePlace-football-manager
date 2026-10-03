package com.eplaceapi.eplace_backend;

import jakarta.validation.constraints.NotBlank;

public record JoinLeagueRequest(
        @NotBlank(message = "Account code is required for M-Pesa verification")
        String accountCode,

        String username
) {}
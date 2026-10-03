package com.eplaceapi.eplace_backend;

import com.eplaceapi.eplace_backend.AwardType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record LeagueCreateRequest(
        @NotBlank(message = "League name cannot be blank")
        @Size(min = 3, max = 50, message = "League name must be between 3 and 50 characters")
        String name,

        @NotBlank(message = "Account code is required for M-Pesa verification")
        String accountCode,

        String creatorUsername,

        @NotNull(message = "Join amount is required")
        @DecimalMin(value = "20.0", inclusive = true, message = "Join amount cannot be negative")
        BigDecimal joinAmount,

        @NotNull(message = "Maximum teams count is required")
        @Min(value = 2, message = "A league must have at least 2 teams")
        @Max(value = 50, message = "League capacity capped at 50 for maximum user experience")
        Integer maxTeams,

        @NotNull(message = "Award type configuration is required")
        AwardType awardType


) {}
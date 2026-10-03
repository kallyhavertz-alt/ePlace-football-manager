package com.eplaceapi.eplace_backend;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LeagueResponse(
        Long id,
        String name,
        String accountCode,
        Long creatorId,
        String creatorUsername,
        BigDecimal joinAmount,
        Integer maxTeams,
        Integer currentTeams,
        AwardType awardType,
        String status,
        LocalDateTime createdAt
) {}
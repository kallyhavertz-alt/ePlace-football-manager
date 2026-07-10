package com.eplaceapi.eplace_backend;

import com.eplaceapi.eplace_backend.AwardType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LeagueResponse(
        Long id,
        String name,
        Long creatorId,
        BigDecimal joinAmount,
        Integer maxTeams,
        AwardType awardType,
        LocalDateTime createdAt
) {}
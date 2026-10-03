package com.eplaceapi.eplace_backend;

import java.math.BigDecimal;

public record PendingAdminRequestDto(
        String id,
        String type,
        String userInitials,
        String username,
        String target,
        BigDecimal amount
) {}
package com.eplaceapi.eplace_backend;

import java.math.BigDecimal;
import java.util.List;

public record PendingPayoutDto(
        String id,
        String title,
        String timeAgo,
        Boolean isMultiWinner,
        BigDecimal amount,
        String phone,
        String winner,
        List<WinnerDetailDto> winners
) {
    public record WinnerDetailDto(
            int rank,
            String name,
            String phone,
            String prize
    ) {}
}
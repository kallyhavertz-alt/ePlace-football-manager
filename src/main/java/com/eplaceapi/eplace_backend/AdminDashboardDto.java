package com.eplaceapi.eplace_backend;

import java.util.List;

public record AdminDashboardDto(
        List<PendingAdminRequestDto> pendingRequests,
        List<PendingPayoutDto> pendingPayouts,
        List<HistoryLogDto> historyLogs
) {}
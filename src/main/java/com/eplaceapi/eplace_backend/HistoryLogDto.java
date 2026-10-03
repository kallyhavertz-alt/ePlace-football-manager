package com.eplaceapi.eplace_backend;

public record HistoryLogDto(
        String type,
        String timeAgo,
        String details
) {}
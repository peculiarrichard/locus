package com.locus.analytics.event;

import java.time.Instant;
import java.util.UUID;

public record SessionCompletedPayload(
    UUID userId,
    UUID sessionId,
    String sessionType,
    Instant startedAt,
    Instant completedAt,
    long durationSeconds,
    UUID goalId) {
}

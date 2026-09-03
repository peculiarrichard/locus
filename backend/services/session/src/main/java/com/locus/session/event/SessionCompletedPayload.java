package com.locus.session.event;

import java.time.Instant;
import java.util.UUID;

// Payload for the SessionCompleted event, per technical-spec.md §7's catalog.
public record SessionCompletedPayload(
    UUID userId,
    UUID sessionId,
    String sessionType,
    Instant startedAt,
    Instant completedAt,
    int durationSeconds,
    UUID goalId) {
}

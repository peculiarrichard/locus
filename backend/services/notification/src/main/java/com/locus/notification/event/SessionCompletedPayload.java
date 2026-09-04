package com.locus.notification.event;

import java.time.Instant;
import java.util.UUID;

// Only userId/completedAt are used here — this service just needs to know a session finished and
// when, to drive the reminder job's "already studied today?" check.
public record SessionCompletedPayload(
    UUID userId,
    UUID sessionId,
    String sessionType,
    Instant startedAt,
    Instant completedAt,
    long durationSeconds,
    UUID goalId) {
}

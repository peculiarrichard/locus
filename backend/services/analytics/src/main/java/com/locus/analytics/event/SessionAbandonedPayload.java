package com.locus.analytics.event;

import java.time.Instant;
import java.util.UUID;

public record SessionAbandonedPayload(
    UUID userId, UUID sessionId, Instant startedAt, Instant abandonedAt, long elapsedSeconds) {
}

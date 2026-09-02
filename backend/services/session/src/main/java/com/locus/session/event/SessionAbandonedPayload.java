package com.locus.session.event;

import java.time.Instant;
import java.util.UUID;

// Payload for the SessionAbandoned event, per technical-spec.md §7's catalog.
public record SessionAbandonedPayload(
    UUID userId, UUID sessionId, Instant startedAt, Instant abandonedAt, int elapsedSeconds) {
}

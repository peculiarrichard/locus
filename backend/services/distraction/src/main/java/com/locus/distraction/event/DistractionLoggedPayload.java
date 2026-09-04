package com.locus.distraction.event;

import java.time.Instant;
import java.util.UUID;

// Per technical-spec.md §7's event catalog — deliberately omits duration_seconds; Analytics'
// distraction-frequency metric normalizes by session focus-seconds, not per-event duration.
public record DistractionLoggedPayload(UUID userId, UUID sessionId, UUID distractionId, Instant occurredAt) {
}

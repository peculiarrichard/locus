package com.locus.analytics.event;

import java.time.Instant;
import java.util.UUID;

public record DistractionLoggedPayload(UUID userId, UUID sessionId, UUID distractionId, Instant occurredAt) {
}

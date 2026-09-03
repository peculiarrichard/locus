package com.locus.auth.event;

import java.time.Instant;
import java.util.UUID;

// Payload for the UserDeleted event, consumed by all 6 non-Auth services to cascade the erasure.
public record UserDeletedPayload(UUID userId, Instant deletedAt) {
}

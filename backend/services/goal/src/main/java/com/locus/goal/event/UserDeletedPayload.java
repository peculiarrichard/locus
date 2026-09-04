package com.locus.goal.event;

import java.time.Instant;
import java.util.UUID;

// Payload for the UserDeleted event this service consumes to purge its own data.
public record UserDeletedPayload(UUID userId, Instant deletedAt) {
}

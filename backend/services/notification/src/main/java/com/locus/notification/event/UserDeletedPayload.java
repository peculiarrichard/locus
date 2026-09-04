package com.locus.notification.event;

import java.time.Instant;
import java.util.UUID;

public record UserDeletedPayload(UUID userId, Instant deletedAt) {
}

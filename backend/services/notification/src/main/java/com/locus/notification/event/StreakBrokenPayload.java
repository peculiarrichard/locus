package com.locus.notification.event;

import java.time.Instant;
import java.util.UUID;

public record StreakBrokenPayload(UUID userId, int streakLengthBeforeBreak, Instant brokenAt) {
}

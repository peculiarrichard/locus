package com.locus.goal.event;

import java.time.Instant;
import java.util.UUID;

// The subset of Session Service's SessionCompleted payload this service needs — only goal_id
// matches trigger the rollup, per frd.md's session-activity-rollup mechanism.
public record SessionCompletedPayload(
    UUID userId,
    UUID sessionId,
    String sessionType,
    Instant startedAt,
    Instant completedAt,
    long durationSeconds,
    UUID goalId) {
}

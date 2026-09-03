package com.locus.notification.event;

import java.time.LocalDate;
import java.util.UUID;

public record GoalDeadlineApproachingPayload(
    UUID userId, UUID goalId, String goalType, LocalDate targetDate, int daysRemaining) {
}

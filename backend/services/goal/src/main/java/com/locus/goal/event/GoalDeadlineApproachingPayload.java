package com.locus.goal.event;

import com.locus.goal.domain.GoalType;
import java.time.LocalDate;
import java.util.UUID;

public record GoalDeadlineApproachingPayload(
    UUID userId, UUID goalId, GoalType goalType, LocalDate targetDate, int daysRemaining) {
}

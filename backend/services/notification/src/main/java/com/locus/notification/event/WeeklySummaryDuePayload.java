package com.locus.notification.event;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record WeeklySummaryDuePayload(
    UUID userId,
    LocalDate weekStart,
    LocalDate weekEnd,
    List<Integer> bestHours,
    double completionRate,
    int distractionCount) {
}

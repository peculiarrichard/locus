package com.locus.analytics.event;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

// Full summary content, not a trigger, per technical-spec.md §7. bestHours is the top hour-of-day
// buckets by focus-seconds within the summary week (a size not specified anywhere — 3 chosen as
// a reasonable "top hours" count for a weekly-summary email).
public record WeeklySummaryDuePayload(
    UUID userId,
    LocalDate weekStart,
    LocalDate weekEnd,
    List<Integer> bestHours,
    double completionRate,
    int distractionCount) {
}

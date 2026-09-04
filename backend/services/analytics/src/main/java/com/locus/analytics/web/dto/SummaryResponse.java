package com.locus.analytics.web.dto;

public record SummaryResponse(
    int currentStreakDays,
    int longestStreakDays,
    int sessionsCompletedThisWeek,
    int sessionsAbandonedThisWeek,
    long totalFocusSecondsThisWeek,
    double completionRateThisWeek) {
}

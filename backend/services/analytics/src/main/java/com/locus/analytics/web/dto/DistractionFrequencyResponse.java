package com.locus.analytics.web.dto;

// distractionsPerFocusHour: distraction_count normalized by total focus-seconds logged in that
// same bucket (converted to hours), per frd.md — a raw count would just track which hours have
// the most total study time, not which hours are actually more distraction-prone.
public record DistractionFrequencyResponse(
    int hourOfDay, int distractionCount, long totalFocusSecondsInBucket, double distractionsPerFocusHour) {
}

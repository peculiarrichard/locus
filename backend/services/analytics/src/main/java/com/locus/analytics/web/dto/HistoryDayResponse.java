package com.locus.analytics.web.dto;

import com.locus.analytics.domain.SessionStatsDaily;
import java.time.LocalDate;

public record HistoryDayResponse(
    LocalDate date, int sessionsCompleted, int sessionsAbandoned, long totalFocusSeconds, int distractionCount) {

  public static HistoryDayResponse from(SessionStatsDaily stats) {
    return new HistoryDayResponse(
        stats.getStatDate(),
        stats.getSessionsCompleted(),
        stats.getSessionsAbandoned(),
        stats.getTotalFocusSeconds(),
        stats.getDistractionCount());
  }
}

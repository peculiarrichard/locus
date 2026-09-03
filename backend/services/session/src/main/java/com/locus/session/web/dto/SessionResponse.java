package com.locus.session.web.dto;

import com.locus.session.domain.Session;
import com.locus.session.domain.SessionStatus;
import com.locus.session.domain.SessionType;
import java.time.Instant;
import java.util.UUID;

// Response shape for every session-returning endpoint.
public record SessionResponse(
    UUID id,
    SessionType sessionType,
    Integer plannedDurationSeconds,
    UUID goalId,
    Integer workMinutes,
    Integer breakMinutes,
    Integer cycleCount,
    Instant startedAt,
    int accumulatedPauseSeconds,
    Instant completedAt,
    Instant abandonedAt,
    Integer durationSeconds,
    SessionStatus status) {

  public static SessionResponse from(Session session) {
    return new SessionResponse(
        session.getId(),
        session.getSessionType(),
        session.getPlannedDurationSeconds(),
        session.getGoalId(),
        session.getWorkMinutes(),
        session.getBreakMinutes(),
        session.getCycleCount(),
        session.getStartedAt(),
        session.getAccumulatedPauseSeconds(),
        session.getCompletedAt(),
        session.getAbandonedAt(),
        session.getDurationSeconds(),
        session.getStatus());
  }
}

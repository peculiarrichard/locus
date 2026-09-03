package com.locus.session.web.dto;

import com.locus.session.domain.SessionType;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

// Request body for POST /sessions/start.
public record StartSessionRequest(
    @NotNull SessionType sessionType,
    Integer plannedDurationSeconds,
    UUID goalId,
    Integer workMinutes,
    Integer breakMinutes,
    Integer cycleCount) {
}

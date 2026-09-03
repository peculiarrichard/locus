package com.locus.distraction.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Instant;
import java.util.UUID;

// Request body for POST /distractions. id is client-generated at blur time, per frd.md, and
// doubles as the idempotency key.
public record LogDistractionRequest(
    @NotNull UUID id,
    @NotNull UUID sessionId,
    @NotNull Instant occurredAt,
    @NotNull @Positive Integer durationSeconds) {
}

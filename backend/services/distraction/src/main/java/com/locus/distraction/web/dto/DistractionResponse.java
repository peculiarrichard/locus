package com.locus.distraction.web.dto;

import com.locus.distraction.domain.DistractionEvent;
import java.time.Instant;
import java.util.UUID;

public record DistractionResponse(UUID id, UUID sessionId, Instant occurredAt, int durationSeconds) {

  public static DistractionResponse from(DistractionEvent event) {
    return new DistractionResponse(
        event.getId(), event.getSessionId(), event.getOccurredAt(), event.getDurationSeconds());
  }
}

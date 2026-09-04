package com.locus.accountability.event;

import java.time.Instant;
import java.util.UUID;

// The standard cross-service event envelope shape from technical-spec.md §7, generic over the payload type.
public record EventEnvelope<T>(
    String eventId,
    String eventType,
    String eventVersion,
    Instant occurredAt,
    String correlationId,
    String producerService,
    T payload) {

  public static <T> EventEnvelope<T> of(String eventType, T payload) {
    return new EventEnvelope<>(
        UUID.randomUUID().toString(),
        eventType,
        "1.0",
        Instant.now(),
        UUID.randomUUID().toString(),
        "accountability-service",
        payload);
  }
}

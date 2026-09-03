package com.locus.notification.event;

import java.time.Instant;

// The standard cross-service event envelope shape from technical-spec.md §7, generic over the
// payload type. No `of()` factory here — this service only ever deserializes envelopes, never
// publishes its own (it has no downstream consumers of its own events).
public record EventEnvelope<T>(
    String eventId,
    String eventType,
    String eventVersion,
    Instant occurredAt,
    String correlationId,
    String producerService,
    T payload) {
}

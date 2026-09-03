package com.locus.analytics.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.locus.analytics.service.AnalyticsIngestService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.stereotype.Component;

@Component
public class SessionAbandonedConsumer {

  private static final TypeReference<EventEnvelope<SessionAbandonedPayload>> EVENT_TYPE = new TypeReference<>() {
  };

  private final AnalyticsIngestService analyticsIngestService;
  private final IdempotencyGuard idempotencyGuard;
  private final ObjectMapper objectMapper;

  public SessionAbandonedConsumer(
      AnalyticsIngestService analyticsIngestService, IdempotencyGuard idempotencyGuard, ObjectMapper objectMapper) {
    this.analyticsIngestService = analyticsIngestService;
    this.idempotencyGuard = idempotencyGuard;
    this.objectMapper = objectMapper;
  }

  @SqsListener("analytics-session-abandoned-queue")
  public void onSessionAbandoned(String rawMessage) throws JsonProcessingException {
    EventEnvelope<SessionAbandonedPayload> event = objectMapper.readValue(rawMessage, EVENT_TYPE);
    if (!idempotencyGuard.claim(event.eventId())) {
      return;
    }
    SessionAbandonedPayload payload = event.payload();
    ConflictRetry.run(() -> analyticsIngestService.recordSessionAbandoned(payload));
  }
}

package com.locus.analytics.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.locus.analytics.service.AnalyticsIngestService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.stereotype.Component;

@Component
public class DistractionLoggedConsumer {

  private static final TypeReference<EventEnvelope<DistractionLoggedPayload>> EVENT_TYPE = new TypeReference<>() {
  };

  private final AnalyticsIngestService analyticsIngestService;
  private final IdempotencyGuard idempotencyGuard;
  private final ObjectMapper objectMapper;

  public DistractionLoggedConsumer(
      AnalyticsIngestService analyticsIngestService, IdempotencyGuard idempotencyGuard, ObjectMapper objectMapper) {
    this.analyticsIngestService = analyticsIngestService;
    this.idempotencyGuard = idempotencyGuard;
    this.objectMapper = objectMapper;
  }

  @SqsListener("analytics-distraction-logged-queue")
  public void onDistractionLogged(String rawMessage) throws JsonProcessingException {
    EventEnvelope<DistractionLoggedPayload> event = objectMapper.readValue(rawMessage, EVENT_TYPE);
    if (!idempotencyGuard.claim(event.eventId())) {
      return;
    }
    DistractionLoggedPayload payload = event.payload();
    ConflictRetry.run(() -> analyticsIngestService.recordDistraction(payload));
  }
}

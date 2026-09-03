package com.locus.notification.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.locus.notification.service.NotificationService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.stereotype.Component;

@Component
public class WeeklySummaryDueConsumer {

  private static final TypeReference<EventEnvelope<WeeklySummaryDuePayload>> EVENT_TYPE = new TypeReference<>() {
  };

  private final NotificationService notificationService;
  private final IdempotencyGuard idempotencyGuard;
  private final ObjectMapper objectMapper;

  public WeeklySummaryDueConsumer(
      NotificationService notificationService, IdempotencyGuard idempotencyGuard, ObjectMapper objectMapper) {
    this.notificationService = notificationService;
    this.idempotencyGuard = idempotencyGuard;
    this.objectMapper = objectMapper;
  }

  @SqsListener("notification-weekly-summary-due-queue")
  public void onWeeklySummaryDue(String rawMessage) throws JsonProcessingException {
    EventEnvelope<WeeklySummaryDuePayload> event = objectMapper.readValue(rawMessage, EVENT_TYPE);
    if (!idempotencyGuard.claim(event.eventId())) {
      return;
    }
    WeeklySummaryDuePayload payload = event.payload();
    notificationService.onWeeklySummaryDue(
        payload.userId(),
        payload.weekStart(),
        payload.weekEnd(),
        payload.bestHours(),
        payload.completionRate(),
        payload.distractionCount());
  }
}

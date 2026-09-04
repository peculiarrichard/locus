package com.locus.notification.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.locus.notification.service.NotificationService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.stereotype.Component;

@Component
public class SessionCompletedConsumer {

  private static final TypeReference<EventEnvelope<SessionCompletedPayload>> EVENT_TYPE = new TypeReference<>() {
  };

  private final NotificationService notificationService;
  private final IdempotencyGuard idempotencyGuard;
  private final ObjectMapper objectMapper;

  public SessionCompletedConsumer(NotificationService notificationService, IdempotencyGuard idempotencyGuard,
      ObjectMapper objectMapper) {
    this.notificationService = notificationService;
    this.idempotencyGuard = idempotencyGuard;
    this.objectMapper = objectMapper;
  }

  @SqsListener("notification-session-completed-queue")
  public void onSessionCompleted(String rawMessage) throws JsonProcessingException {
    EventEnvelope<SessionCompletedPayload> event = objectMapper.readValue(rawMessage, EVENT_TYPE);
    if (!idempotencyGuard.claim(event.eventId())) {
      return;
    }
    SessionCompletedPayload payload = event.payload();
    notificationService.onSessionCompleted(payload.userId(), payload.completedAt());
  }
}

package com.locus.notification.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.locus.notification.service.NotificationService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetRequestedConsumer {

  private static final TypeReference<EventEnvelope<PasswordResetRequestedPayload>> EVENT_TYPE = new TypeReference<>() {
  };

  private final NotificationService notificationService;
  private final IdempotencyGuard idempotencyGuard;
  private final ObjectMapper objectMapper;

  public PasswordResetRequestedConsumer(
      NotificationService notificationService, IdempotencyGuard idempotencyGuard, ObjectMapper objectMapper) {
    this.notificationService = notificationService;
    this.idempotencyGuard = idempotencyGuard;
    this.objectMapper = objectMapper;
  }

  @SqsListener("notification-password-reset-requested-queue")
  public void onPasswordResetRequested(String rawMessage) throws JsonProcessingException {
    EventEnvelope<PasswordResetRequestedPayload> event = objectMapper.readValue(rawMessage, EVENT_TYPE);
    if (!idempotencyGuard.claim(event.eventId())) {
      return;
    }
    PasswordResetRequestedPayload payload = event.payload();
    notificationService.onPasswordResetRequested(payload.userId(), payload.email(), payload.resetToken());
  }
}

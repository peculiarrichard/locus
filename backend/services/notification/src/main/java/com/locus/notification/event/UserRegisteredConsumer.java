package com.locus.notification.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.locus.notification.service.NotificationService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.stereotype.Component;

@Component
public class UserRegisteredConsumer {

  private static final TypeReference<EventEnvelope<UserRegisteredPayload>> EVENT_TYPE = new TypeReference<>() {
  };

  private final NotificationService notificationService;
  private final IdempotencyGuard idempotencyGuard;
  private final ObjectMapper objectMapper;

  public UserRegisteredConsumer(NotificationService notificationService, IdempotencyGuard idempotencyGuard,
      ObjectMapper objectMapper) {
    this.notificationService = notificationService;
    this.idempotencyGuard = idempotencyGuard;
    this.objectMapper = objectMapper;
  }

  @SqsListener("notification-user-registered-queue")
  public void onUserRegistered(String rawMessage) throws JsonProcessingException {
    EventEnvelope<UserRegisteredPayload> event = objectMapper.readValue(rawMessage, EVENT_TYPE);
    if (!idempotencyGuard.claim(event.eventId())) {
      return;
    }
    UserRegisteredPayload payload = event.payload();
    notificationService.onUserRegistered(payload.userId(), payload.email(), payload.displayName(),
        payload.verificationToken());
  }
}

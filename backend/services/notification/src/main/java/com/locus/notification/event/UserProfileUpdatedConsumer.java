package com.locus.notification.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.locus.notification.service.NotificationService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.stereotype.Component;

@Component
public class UserProfileUpdatedConsumer {

  private static final TypeReference<EventEnvelope<UserProfileUpdatedPayload>> EVENT_TYPE = new TypeReference<>() {
  };

  private final NotificationService notificationService;
  private final IdempotencyGuard idempotencyGuard;
  private final ObjectMapper objectMapper;

  public UserProfileUpdatedConsumer(
      NotificationService notificationService, IdempotencyGuard idempotencyGuard, ObjectMapper objectMapper) {
    this.notificationService = notificationService;
    this.idempotencyGuard = idempotencyGuard;
    this.objectMapper = objectMapper;
  }

  @SqsListener("notification-user-profile-updated-queue")
  public void onUserProfileUpdated(String rawMessage) throws JsonProcessingException {
    EventEnvelope<UserProfileUpdatedPayload> event = objectMapper.readValue(rawMessage, EVENT_TYPE);
    if (!idempotencyGuard.claim(event.eventId())) {
      return;
    }
    UserProfileUpdatedPayload payload = event.payload();
    notificationService.onUserProfileUpdated(payload.userId(), payload.email(), payload.displayName(),
        payload.timezone());
  }
}

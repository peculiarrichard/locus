package com.locus.notification.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.locus.notification.service.NotificationService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.stereotype.Component;

// Consumes UserDeleted to purge this user's contact/activity/pending-partner-activity data, per
// technical-spec.md §9's erasure cascade.
@Component
public class UserDeletedConsumer {

  private static final TypeReference<EventEnvelope<UserDeletedPayload>> EVENT_TYPE = new TypeReference<>() {
  };

  private final NotificationService notificationService;
  private final IdempotencyGuard idempotencyGuard;
  private final ObjectMapper objectMapper;

  public UserDeletedConsumer(NotificationService notificationService, IdempotencyGuard idempotencyGuard,
      ObjectMapper objectMapper) {
    this.notificationService = notificationService;
    this.idempotencyGuard = idempotencyGuard;
    this.objectMapper = objectMapper;
  }

  @SqsListener("notification-user-deleted-queue")
  public void onUserDeleted(String rawMessage) throws JsonProcessingException {
    EventEnvelope<UserDeletedPayload> event = objectMapper.readValue(rawMessage, EVENT_TYPE);
    if (!idempotencyGuard.claim(event.eventId())) {
      return;
    }
    notificationService.purgeUser(event.payload().userId());
  }
}

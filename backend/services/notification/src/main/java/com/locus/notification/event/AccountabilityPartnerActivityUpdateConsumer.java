package com.locus.notification.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.locus.notification.service.NotificationService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.stereotype.Component;

@Component
public class AccountabilityPartnerActivityUpdateConsumer {

  private static final TypeReference<EventEnvelope<AccountabilityPartnerActivityUpdatePayload>> EVENT_TYPE;

  static {
    EVENT_TYPE = new TypeReference<>() {
    };
  }

  private final NotificationService notificationService;
  private final IdempotencyGuard idempotencyGuard;
  private final ObjectMapper objectMapper;

  public AccountabilityPartnerActivityUpdateConsumer(
      NotificationService notificationService, IdempotencyGuard idempotencyGuard, ObjectMapper objectMapper) {
    this.notificationService = notificationService;
    this.idempotencyGuard = idempotencyGuard;
    this.objectMapper = objectMapper;
  }

  @SqsListener("notification-accountability-partner-activity-update-queue")
  public void onPartnerActivity(String rawMessage) throws JsonProcessingException {
    EventEnvelope<AccountabilityPartnerActivityUpdatePayload> event = objectMapper.readValue(rawMessage, EVENT_TYPE);
    if (!idempotencyGuard.claim(event.eventId())) {
      return;
    }
    AccountabilityPartnerActivityUpdatePayload payload = event.payload();
    notificationService.onPartnerActivity(
        payload.userId(), payload.partnerUserId(), payload.activityType(), payload.occurredAt());
  }
}

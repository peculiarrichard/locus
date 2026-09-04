package com.locus.notification.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.locus.notification.service.NotificationService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.stereotype.Component;

// Consumes the local stub bounce topic — see SesBounceNotificationPayload for why this exists
// as a stand-in rather than a real SES→SNS subscription (that's Part 2 infra).
@Component
public class SesBounceNotificationConsumer {

  private static final TypeReference<EventEnvelope<SesBounceNotificationPayload>> EVENT_TYPE = new TypeReference<>() {
  };

  private final NotificationService notificationService;
  private final IdempotencyGuard idempotencyGuard;
  private final ObjectMapper objectMapper;

  public SesBounceNotificationConsumer(
      NotificationService notificationService, IdempotencyGuard idempotencyGuard, ObjectMapper objectMapper) {
    this.notificationService = notificationService;
    this.idempotencyGuard = idempotencyGuard;
    this.objectMapper = objectMapper;
  }

  @SqsListener("notification-ses-bounce-notification-queue")
  public void onSesBounce(String rawMessage) throws JsonProcessingException {
    EventEnvelope<SesBounceNotificationPayload> event = objectMapper.readValue(rawMessage, EVENT_TYPE);
    if (!idempotencyGuard.claim(event.eventId())) {
      return;
    }
    SesBounceNotificationPayload payload = event.payload();
    notificationService.onSesBounce(payload.email(), payload.bounceType());
  }
}

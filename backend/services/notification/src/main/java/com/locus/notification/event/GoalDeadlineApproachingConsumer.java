package com.locus.notification.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.locus.notification.service.NotificationService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.stereotype.Component;

@Component
public class GoalDeadlineApproachingConsumer {

  private static final TypeReference<EventEnvelope<GoalDeadlineApproachingPayload>> EVENT_TYPE = new TypeReference<>() {
  };

  private final NotificationService notificationService;
  private final IdempotencyGuard idempotencyGuard;
  private final ObjectMapper objectMapper;

  public GoalDeadlineApproachingConsumer(
      NotificationService notificationService, IdempotencyGuard idempotencyGuard, ObjectMapper objectMapper) {
    this.notificationService = notificationService;
    this.idempotencyGuard = idempotencyGuard;
    this.objectMapper = objectMapper;
  }

  @SqsListener("notification-goal-deadline-approaching-queue")
  public void onGoalDeadlineApproaching(String rawMessage) throws JsonProcessingException {
    EventEnvelope<GoalDeadlineApproachingPayload> event = objectMapper.readValue(rawMessage, EVENT_TYPE);
    if (!idempotencyGuard.claim(event.eventId())) {
      return;
    }
    GoalDeadlineApproachingPayload payload = event.payload();
    notificationService.onGoalDeadlineApproaching(payload.userId(), payload.goalType(), payload.daysRemaining());
  }
}

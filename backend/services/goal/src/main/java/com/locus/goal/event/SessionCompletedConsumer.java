package com.locus.goal.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.locus.goal.domain.GoalSessionActivity;
import com.locus.goal.repository.GoalRepository;
import com.locus.goal.repository.GoalSessionActivityRepository;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// Consumes SessionCompleted to keep goal_session_activity current, per frd.md's session-activity
// rollup mechanism. goal_id is opaque to Session Service (never validated there), so an event
// referencing a goal_id this service doesn't recognize (deleted goal, or none supplied) is a
// silent no-op, not an error. Idempotency-guarded: this rollup increments a counter, which is not
// naturally idempotent under SQS's at-least-once redelivery — see IdempotencyGuard.
@Component
public class SessionCompletedConsumer {

  private static final TypeReference<EventEnvelope<SessionCompletedPayload>> EVENT_TYPE = new TypeReference<>() {
  };

  private final GoalRepository goalRepository;
  private final GoalSessionActivityRepository goalSessionActivityRepository;
  private final IdempotencyGuard idempotencyGuard;
  private final ObjectMapper objectMapper;

  public SessionCompletedConsumer(
      GoalRepository goalRepository,
      GoalSessionActivityRepository goalSessionActivityRepository,
      IdempotencyGuard idempotencyGuard,
      ObjectMapper objectMapper) {
    this.goalRepository = goalRepository;
    this.goalSessionActivityRepository = goalSessionActivityRepository;
    this.idempotencyGuard = idempotencyGuard;
    this.objectMapper = objectMapper;
  }

  @SqsListener("goal-session-completed-queue")
  public void onSessionCompleted(String rawMessage) throws JsonProcessingException {
    EventEnvelope<SessionCompletedPayload> event = objectMapper.readValue(rawMessage, EVENT_TYPE);
    if (!idempotencyGuard.claim(event.eventId())) {
      return;
    }
    recordRollup(event.payload());
  }

  @Transactional
  void recordRollup(SessionCompletedPayload payload) {
    if (payload.goalId() == null || !goalRepository.existsById(payload.goalId())) {
      return;
    }
    GoalSessionActivity activity = goalSessionActivityRepository
        .findById(payload.goalId())
        .orElseGet(() -> new GoalSessionActivity(payload.goalId()));
    activity.recordSession(payload.durationSeconds());
    goalSessionActivityRepository.save(activity);
  }
}

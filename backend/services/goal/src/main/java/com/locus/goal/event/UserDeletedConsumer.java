package com.locus.goal.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.locus.goal.repository.GoalRepository;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// Consumes UserDeleted to purge this user's goal data, per technical-spec.md §9's erasure
// cascade. goal_milestones/goal_session_activity/goal_deadline_notifications cascade via FK
// ON DELETE CASCADE, so deleting the goals rows is sufficient. Deserializes the raw body
// manually via TypeReference — see Session/Distraction Services' UserDeletedConsumer for why a
// generic EventEnvelope<T> parameter can't be used directly with @SqsListener.
@Component
public class UserDeletedConsumer {

  private static final TypeReference<EventEnvelope<UserDeletedPayload>> EVENT_TYPE = new TypeReference<>() {
  };

  private final GoalRepository goalRepository;
  private final ObjectMapper objectMapper;

  public UserDeletedConsumer(GoalRepository goalRepository, ObjectMapper objectMapper) {
    this.goalRepository = goalRepository;
    this.objectMapper = objectMapper;
  }

  @SqsListener("goal-user-deleted-queue")
  @Transactional
  public void onUserDeleted(String rawMessage) throws JsonProcessingException {
    EventEnvelope<UserDeletedPayload> event = objectMapper.readValue(rawMessage, EVENT_TYPE);
    goalRepository.deleteByUserId(event.payload().userId());
  }
}

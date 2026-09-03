package com.locus.goal.event;

import io.awspring.cloud.sns.core.SnsHeaders;
import io.awspring.cloud.sns.core.SnsTemplate;
import java.util.Map;
import org.springframework.stereotype.Component;

// Publishes Goal and Plan Service's events to their SNS topics, per technical-spec.md §7's topic-per-event-type pattern.
@Component
public class EventPublisher {

  private final SnsTemplate snsTemplate;

  public EventPublisher(SnsTemplate snsTemplate) {
    this.snsTemplate = snsTemplate;
  }

  // Sends correlationId as a real SNS/SQS message attribute (not just embedded in the payload
  // body), per technical-spec.md §7 — see Auth Service's EventPublisher for the full reasoning.
  public void publishGoalDeadlineApproaching(GoalDeadlineApproachingPayload payload) {
    EventEnvelope<GoalDeadlineApproachingPayload> envelope = EventEnvelope.of("GoalDeadlineApproaching", payload);
    snsTemplate.convertAndSend(
        "goal-deadline-approaching",
        envelope,
        Map.of(
            SnsHeaders.NOTIFICATION_SUBJECT_HEADER, "GoalDeadlineApproaching", "correlationId",
            envelope.correlationId()));
  }
}

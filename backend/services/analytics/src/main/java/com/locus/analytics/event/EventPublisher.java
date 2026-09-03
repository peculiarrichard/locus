package com.locus.analytics.event;

import io.awspring.cloud.sns.core.SnsHeaders;
import io.awspring.cloud.sns.core.SnsTemplate;
import java.util.Map;
import org.springframework.stereotype.Component;

// Publishes Analytics Service's events to their SNS topics, per technical-spec.md §7's topic-per-event-type pattern.
@Component
public class EventPublisher {

  private final SnsTemplate snsTemplate;

  public EventPublisher(SnsTemplate snsTemplate) {
    this.snsTemplate = snsTemplate;
  }

  public void publishStreakBroken(StreakBrokenPayload payload) {
    publish("streak-broken", "StreakBroken", payload);
  }

  public void publishWeeklySummaryDue(WeeklySummaryDuePayload payload) {
    publish("weekly-summary-due", "WeeklySummaryDue", payload);
  }

  // Sends correlationId as a real SNS/SQS message attribute (not just embedded in the payload
  // body), per technical-spec.md §7 — see Auth Service's EventPublisher for the full reasoning.
  private <T> void publish(String topicName, String eventType, T payload) {
    EventEnvelope<T> envelope = EventEnvelope.of(eventType, payload);
    snsTemplate.convertAndSend(
        topicName,
        envelope,
        Map.of(SnsHeaders.NOTIFICATION_SUBJECT_HEADER, eventType, "correlationId", envelope.correlationId()));
  }
}

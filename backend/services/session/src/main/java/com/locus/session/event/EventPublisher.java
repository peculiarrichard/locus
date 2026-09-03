package com.locus.session.event;

import io.awspring.cloud.sns.core.SnsTemplate;
import org.springframework.stereotype.Component;

// Publishes Session Service's events to their SNS topics, per technical-spec.md §7's topic-per-event-type pattern.
@Component
public class EventPublisher {

  private final SnsTemplate snsTemplate;

  public EventPublisher(SnsTemplate snsTemplate) {
    this.snsTemplate = snsTemplate;
  }

  public void publishSessionCompleted(SessionCompletedPayload payload) {
    publish("session-completed", "SessionCompleted", payload);
  }

  public void publishSessionAbandoned(SessionAbandonedPayload payload) {
    publish("session-abandoned", "SessionAbandoned", payload);
  }

  private <T> void publish(String topicName, String eventType, T payload) {
    snsTemplate.sendNotification(topicName, EventEnvelope.of(eventType, payload), eventType);
  }
}

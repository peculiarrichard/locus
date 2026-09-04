package com.locus.auth.event;

import io.awspring.cloud.sns.core.SnsHeaders;
import io.awspring.cloud.sns.core.SnsTemplate;
import java.util.Map;
import org.springframework.stereotype.Component;

// Publishes Auth Service's events to their SNS topics, per technical-spec.md §7's topic-per-event-type pattern.
@Component
public class EventPublisher {

  private final SnsTemplate snsTemplate;

  public EventPublisher(SnsTemplate snsTemplate) {
    this.snsTemplate = snsTemplate;
  }

  public void publishUserRegistered(UserRegisteredPayload payload) {
    publish("user-registered", "UserRegistered", payload);
  }

  public void publishPasswordResetRequested(PasswordResetRequestedPayload payload) {
    publish("password-reset-requested", "PasswordResetRequested", payload);
  }

  public void publishUserProfileUpdated(UserProfileUpdatedPayload payload) {
    publish("user-profile-updated", "UserProfileUpdated", payload);
  }

  public void publishUserDeleted(UserDeletedPayload payload) {
    publish("user-deleted", "UserDeleted", payload);
  }

  // Sends correlationId as a real SNS/SQS message attribute (not just embedded in the payload
  // body), per technical-spec.md §7 — convertAndSend's header map is what Spring Cloud AWS's SNS
  // converter maps onto MessageAttributes; sendNotification's plain (topic, payload, subject)
  // overload has no way to carry extra attributes at all.
  private <T> void publish(String topicName, String eventType, T payload) {
    EventEnvelope<T> envelope = EventEnvelope.of(eventType, payload);
    snsTemplate.convertAndSend(
        topicName,
        envelope,
        Map.of(SnsHeaders.NOTIFICATION_SUBJECT_HEADER, eventType, "correlationId", envelope.correlationId()));
  }
}

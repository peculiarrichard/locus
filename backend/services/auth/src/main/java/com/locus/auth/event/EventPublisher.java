package com.locus.auth.event;

import io.awspring.cloud.sns.core.SnsTemplate;
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

  private <T> void publish(String topicName, String eventType, T payload) {
    snsTemplate.sendNotification(topicName, EventEnvelope.of(eventType, payload), eventType);
  }
}

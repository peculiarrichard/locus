package com.locus.distraction.event;

import io.awspring.cloud.sns.core.SnsHeaders;
import io.awspring.cloud.sns.core.SnsTemplate;
import java.util.Map;
import org.springframework.stereotype.Component;

// Publishes Distraction Logging Service's events to their SNS topics, per technical-spec.md §7's topic-per-event-type pattern.
@Component
public class EventPublisher {

  private final SnsTemplate snsTemplate;

  public EventPublisher(SnsTemplate snsTemplate) {
    this.snsTemplate = snsTemplate;
  }

  // Sends correlationId as a real SNS/SQS message attribute (not just embedded in the payload
  // body), per technical-spec.md §7 — see Auth Service's EventPublisher for the full reasoning.
  public void publishDistractionLogged(DistractionLoggedPayload payload) {
    EventEnvelope<DistractionLoggedPayload> envelope = EventEnvelope.of("DistractionLogged", payload);
    snsTemplate.convertAndSend(
        "distraction-logged",
        envelope,
        Map.of(SnsHeaders.NOTIFICATION_SUBJECT_HEADER, "DistractionLogged", "correlationId", envelope.correlationId()));
  }
}

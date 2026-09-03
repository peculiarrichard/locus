package com.locus.distraction.event;

import io.awspring.cloud.sns.core.SnsTemplate;
import org.springframework.stereotype.Component;

// Publishes Distraction Logging Service's events to their SNS topics, per technical-spec.md §7's topic-per-event-type pattern.
@Component
public class EventPublisher {

  private final SnsTemplate snsTemplate;

  public EventPublisher(SnsTemplate snsTemplate) {
    this.snsTemplate = snsTemplate;
  }

  public void publishDistractionLogged(DistractionLoggedPayload payload) {
    snsTemplate.sendNotification(
        "distraction-logged", EventEnvelope.of("DistractionLogged", payload), "DistractionLogged");
  }
}

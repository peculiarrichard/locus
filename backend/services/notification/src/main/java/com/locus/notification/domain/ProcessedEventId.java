package com.locus.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

// Idempotency claim per consumed event, per frd.md — applied uniformly to every consumer, since
// unlike Distraction Logging's client-generated id or Session's DB constraint, none of the
// events this service consumes carry their own natural dedupe key.
@Entity
@Table(name = "processed_event_ids")
public class ProcessedEventId {

  @Id
  @Column(name = "event_id")
  private String eventId;

  @Column(name = "processed_at", nullable = false)
  private Instant processedAt = Instant.now();

  protected ProcessedEventId() {
  }

  public ProcessedEventId(String eventId) {
    this.eventId = eventId;
  }

  public String getEventId() {
    return eventId;
  }
}

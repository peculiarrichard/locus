package com.locus.analytics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

// Idempotency claim for consumers whose effect isn't naturally idempotent under SQS's
// at-least-once redelivery (counter increments) — see V2__idempotency.sql.
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

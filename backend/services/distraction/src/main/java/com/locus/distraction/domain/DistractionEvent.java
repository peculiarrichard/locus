package com.locus.distraction.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

// JPA entity for distraction_events — id is client-generated at blur time, per frd.md, and doubles
// as the idempotency key for submission retries.
@Entity
@Table(name = "distraction_events")
public class DistractionEvent {

  @Id
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "session_id", nullable = false)
  private UUID sessionId;

  @Column(name = "occurred_at", nullable = false)
  private Instant occurredAt;

  @Column(name = "duration_seconds", nullable = false)
  private int durationSeconds;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  protected DistractionEvent() {
  }

  public DistractionEvent(UUID id, UUID userId, UUID sessionId, Instant occurredAt, int durationSeconds) {
    this.id = id;
    this.userId = userId;
    this.sessionId = sessionId;
    this.occurredAt = occurredAt;
    this.durationSeconds = durationSeconds;
  }

  public UUID getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public UUID getSessionId() {
    return sessionId;
  }

  public Instant getOccurredAt() {
    return occurredAt;
  }

  public int getDurationSeconds() {
    return durationSeconds;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}

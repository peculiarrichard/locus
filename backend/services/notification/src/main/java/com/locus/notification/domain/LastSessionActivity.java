package com.locus.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

// Per frd.md's reminder-scheduling gap: drives the per-user reminder job's "already studied
// today?" check without a synchronous call to Session Service.
@Entity
@Table(name = "last_session_activity")
public class LastSessionActivity {

  @Id
  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "last_completed_at", nullable = false)
  private Instant lastCompletedAt;

  protected LastSessionActivity() {
  }

  public LastSessionActivity(UUID userId, Instant lastCompletedAt) {
    this.userId = userId;
    this.lastCompletedAt = lastCompletedAt;
  }

  public UUID getUserId() {
    return userId;
  }

  public Instant getLastCompletedAt() {
    return lastCompletedAt;
  }

  public void setLastCompletedAt(Instant lastCompletedAt) {
    this.lastCompletedAt = lastCompletedAt;
  }
}

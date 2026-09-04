package com.locus.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

// Per-send record for support/debugging ("I never got my reset email"), per frd.md.
@Entity
@Table(name = "notification_log")
public class NotificationLog {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "notification_type", nullable = false)
  private String notificationType;

  @Column(name = "sent_at", nullable = false)
  private Instant sentAt = Instant.now();

  protected NotificationLog() {
  }

  public NotificationLog(UUID userId, String notificationType) {
    this.userId = userId;
    this.notificationType = notificationType;
  }

  public UUID getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public String getNotificationType() {
    return notificationType;
  }

  public Instant getSentAt() {
    return sentAt;
  }
}

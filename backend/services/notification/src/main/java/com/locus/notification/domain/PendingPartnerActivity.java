package com.locus.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

// Batched session_completed partner activity awaiting the next weekly summary — see V1__init.sql.
@Entity
@Table(name = "pending_partner_activity")
public class PendingPartnerActivity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "partner_user_id", nullable = false)
  private UUID partnerUserId;

  @Column(name = "activity_type", nullable = false)
  private String activityType;

  @Column(name = "occurred_at", nullable = false)
  private Instant occurredAt;

  protected PendingPartnerActivity() {
  }

  public PendingPartnerActivity(UUID userId, UUID partnerUserId, String activityType, Instant occurredAt) {
    this.userId = userId;
    this.partnerUserId = partnerUserId;
    this.activityType = activityType;
    this.occurredAt = occurredAt;
  }

  public UUID getUserId() {
    return userId;
  }

  public UUID getPartnerUserId() {
    return partnerUserId;
  }

  public String getActivityType() {
    return activityType;
  }

  public Instant getOccurredAt() {
    return occurredAt;
  }
}

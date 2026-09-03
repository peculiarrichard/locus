package com.locus.accountability.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

// The pre-membership "invited" state from frd.md's status enum lives here, as a shareable code,
// rather than as a member-table row — see V1__init.sql.
@Entity
@Table(name = "accountability_invites")
public class AccountabilityInvite {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true)
  private String code;

  @Column(name = "group_id", nullable = false)
  private UUID groupId;

  @Column(name = "created_by", nullable = false)
  private UUID createdBy;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private InviteStatus status = InviteStatus.PENDING;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  protected AccountabilityInvite() {
  }

  public AccountabilityInvite(String code, UUID groupId, UUID createdBy, Instant expiresAt) {
    this.code = code;
    this.groupId = groupId;
    this.createdBy = createdBy;
    this.expiresAt = expiresAt;
  }

  public UUID getId() {
    return id;
  }

  public String getCode() {
    return code;
  }

  public UUID getGroupId() {
    return groupId;
  }

  public UUID getCreatedBy() {
    return createdBy;
  }

  public InviteStatus getStatus() {
    return status;
  }

  public void setStatus(InviteStatus status) {
    this.status = status;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public boolean isUsable() {
    return status == InviteStatus.PENDING && expiresAt.isAfter(Instant.now());
  }
}

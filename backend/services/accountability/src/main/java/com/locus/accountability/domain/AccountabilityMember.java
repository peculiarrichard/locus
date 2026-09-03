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

@Entity
@Table(name = "accountability_members")
public class AccountabilityMember {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "group_id", nullable = false)
  private UUID groupId;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "joined_at", nullable = false, updatable = false)
  private Instant joinedAt = Instant.now();

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MemberStatus status = MemberStatus.ACTIVE;

  @Column(name = "left_at")
  private Instant leftAt;

  protected AccountabilityMember() {
  }

  public AccountabilityMember(UUID groupId, UUID userId) {
    this.groupId = groupId;
    this.userId = userId;
  }

  public UUID getId() {
    return id;
  }

  public UUID getGroupId() {
    return groupId;
  }

  public UUID getUserId() {
    return userId;
  }

  public Instant getJoinedAt() {
    return joinedAt;
  }

  public MemberStatus getStatus() {
    return status;
  }

  public void leave() {
    this.status = MemberStatus.LEFT;
    this.leftAt = Instant.now();
  }

  public boolean isActive() {
    return status == MemberStatus.ACTIVE;
  }
}

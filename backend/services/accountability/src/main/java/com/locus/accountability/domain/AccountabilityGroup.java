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

// JPA entity for accountability_groups, per frd.md's Accountability Service section.
@Entity
@Table(name = "accountability_groups")
public class AccountabilityGroup {

  // PAIR groups are capped at 2 members; a pair dissolves entirely rather than continuing at 1
  // member, since a "pair" with one person no longer means anything.
  public static final int PAIR_CAPACITY = 2;
  public static final int GROUP_CAPACITY = 10;

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(name = "group_type", nullable = false)
  private GroupType groupType;

  @Column(name = "created_by", nullable = false)
  private UUID createdBy;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  protected AccountabilityGroup() {
  }

  public AccountabilityGroup(GroupType groupType, UUID createdBy) {
    this.groupType = groupType;
    this.createdBy = createdBy;
  }

  public UUID getId() {
    return id;
  }

  public GroupType getGroupType() {
    return groupType;
  }

  public UUID getCreatedBy() {
    return createdBy;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public int capacity() {
    return groupType == GroupType.PAIR ? PAIR_CAPACITY : GROUP_CAPACITY;
  }
}

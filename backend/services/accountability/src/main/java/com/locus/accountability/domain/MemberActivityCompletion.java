package com.locus.accountability.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

// Presence of a row means "this user completed a qualifying session this day" — absence means no,
// per frd.md's "no row means no" derivation rule. Only written for users active in at least one
// group, per the independence guarantee (see V1__init.sql).
@Entity
@Table(name = "member_activity_completions")
@IdClass(MemberActivityCompletion.Key.class)
public class MemberActivityCompletion {

  @Id
  @Column(name = "user_id")
  private UUID userId;

  @Id
  @Column(name = "completed_date")
  private LocalDate completedDate;

  protected MemberActivityCompletion() {
  }

  public MemberActivityCompletion(UUID userId, LocalDate completedDate) {
    this.userId = userId;
    this.completedDate = completedDate;
  }

  public UUID getUserId() {
    return userId;
  }

  public LocalDate getCompletedDate() {
    return completedDate;
  }

  public static class Key implements Serializable {
    private UUID userId;
    private LocalDate completedDate;

    public Key() {
    }

    public Key(UUID userId, LocalDate completedDate) {
      this.userId = userId;
      this.completedDate = completedDate;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Key key)) {
        return false;
      }
      return Objects.equals(userId, key.userId) && Objects.equals(completedDate, key.completedDate);
    }

    @Override
    public int hashCode() {
      return Objects.hash(userId, completedDate);
    }
  }
}

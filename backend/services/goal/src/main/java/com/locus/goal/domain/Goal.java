package com.locus.goal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

// JPA entity for the goals table, per frd.md's Goal and Plan Service section.
@Entity
@Table(name = "goals")
public class Goal {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Enumerated(EnumType.STRING)
  @Column(name = "goal_type", nullable = false)
  private GoalType goalType;

  @Column(nullable = false)
  private String title;

  @Column(name = "target_date", nullable = false)
  private LocalDate targetDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private GoalStatus status = GoalStatus.ACTIVE;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  protected Goal() {
  }

  public Goal(UUID userId, GoalType goalType, String title, LocalDate targetDate) {
    this.userId = userId;
    this.goalType = goalType;
    this.title = title;
    this.targetDate = targetDate;
  }

  public UUID getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public GoalType getGoalType() {
    return goalType;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public LocalDate getTargetDate() {
    return targetDate;
  }

  public void setTargetDate(LocalDate targetDate) {
    this.targetDate = targetDate;
  }

  public GoalStatus getStatus() {
    return status;
  }

  public void setStatus(GoalStatus status) {
    this.status = status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public boolean isTerminal() {
    return status != GoalStatus.ACTIVE;
  }
}

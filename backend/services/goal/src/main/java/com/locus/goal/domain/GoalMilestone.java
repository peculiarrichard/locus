package com.locus.goal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

// JPA entity for goal_milestones — copied from a PlanTemplate at goal-creation time, per frd.md's
// copy-on-create requirement.
@Entity
@Table(name = "goal_milestones")
public class GoalMilestone {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "goal_id", nullable = false)
  private UUID goalId;

  @Column(name = "milestone_name", nullable = false)
  private String milestoneName;

  private String description;

  @Column(name = "milestone_offset_days", nullable = false)
  private int milestoneOffsetDays;

  @Column(name = "due_date", nullable = false)
  private LocalDate dueDate;

  @Column(name = "completed_at")
  private Instant completedAt;

  protected GoalMilestone() {
  }

  public GoalMilestone(
      UUID goalId, String milestoneName, String description, int milestoneOffsetDays, LocalDate dueDate) {
    this.goalId = goalId;
    this.milestoneName = milestoneName;
    this.description = description;
    this.milestoneOffsetDays = milestoneOffsetDays;
    this.dueDate = dueDate;
  }

  public UUID getId() {
    return id;
  }

  public UUID getGoalId() {
    return goalId;
  }

  public String getMilestoneName() {
    return milestoneName;
  }

  public String getDescription() {
    return description;
  }

  public int getMilestoneOffsetDays() {
    return milestoneOffsetDays;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public void setDueDate(LocalDate dueDate) {
    this.dueDate = dueDate;
  }

  public Instant getCompletedAt() {
    return completedAt;
  }

  public void setCompletedAt(Instant completedAt) {
    this.completedAt = completedAt;
  }

  public boolean isCompleted() {
    return completedAt != null;
  }
}

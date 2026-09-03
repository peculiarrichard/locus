package com.locus.goal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

// Tracks which deadline thresholds (30/14/7/1 days) have already fired for a goal, per frd.md's
// "a set, not a single flag" requirement.
@Entity
@Table(name = "goal_deadline_notifications")
@IdClass(GoalDeadlineNotification.Key.class)
public class GoalDeadlineNotification {

  @Id
  @Column(name = "goal_id")
  private UUID goalId;

  @Id
  @Column(name = "threshold_days")
  private int thresholdDays;

  protected GoalDeadlineNotification() {
  }

  public GoalDeadlineNotification(UUID goalId, int thresholdDays) {
    this.goalId = goalId;
    this.thresholdDays = thresholdDays;
  }

  public UUID getGoalId() {
    return goalId;
  }

  public int getThresholdDays() {
    return thresholdDays;
  }

  public static class Key implements Serializable {
    private UUID goalId;
    private int thresholdDays;

    public Key() {
    }

    public Key(UUID goalId, int thresholdDays) {
      this.goalId = goalId;
      this.thresholdDays = thresholdDays;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Key key)) {
        return false;
      }
      return thresholdDays == key.thresholdDays && Objects.equals(goalId, key.goalId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(goalId, thresholdDays);
    }
  }
}

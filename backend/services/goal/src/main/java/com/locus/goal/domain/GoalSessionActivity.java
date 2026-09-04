package com.locus.goal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

// Denormalized session-activity rollup per goal, kept current by consuming SessionCompleted
// events where goal_id matches, per frd.md's second progress-tracking mechanism.
@Entity
@Table(name = "goal_session_activity")
public class GoalSessionActivity {

  @Id
  @Column(name = "goal_id")
  private UUID goalId;

  @Column(name = "total_sessions", nullable = false)
  private int totalSessions;

  @Column(name = "total_duration_seconds", nullable = false)
  private long totalDurationSeconds;

  protected GoalSessionActivity() {
  }

  public GoalSessionActivity(UUID goalId) {
    this.goalId = goalId;
    this.totalSessions = 0;
    this.totalDurationSeconds = 0;
  }

  public UUID getGoalId() {
    return goalId;
  }

  public int getTotalSessions() {
    return totalSessions;
  }

  public long getTotalDurationSeconds() {
    return totalDurationSeconds;
  }

  public void recordSession(long durationSeconds) {
    this.totalSessions += 1;
    this.totalDurationSeconds += durationSeconds;
  }
}

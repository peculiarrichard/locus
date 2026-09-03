package com.locus.session.domain;

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

// JPA entity for the sessions table — a focus session's full lifecycle, per frd.md's Session Service section.
@Entity
@Table(name = "sessions")
public class Session {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Enumerated(EnumType.STRING)
  @Column(name = "session_type", nullable = false)
  private SessionType sessionType;

  @Column(name = "planned_duration_seconds")
  private Integer plannedDurationSeconds;

  // Deliberately opaque per frd.md — never resolved or validated against Goal and Plan Service.
  @Column(name = "goal_id")
  private UUID goalId;

  @Column(name = "work_minutes")
  private Integer workMinutes;

  @Column(name = "break_minutes")
  private Integer breakMinutes;

  @Column(name = "cycle_count")
  private Integer cycleCount;

  @Column(name = "started_at", nullable = false, updatable = false)
  private Instant startedAt = Instant.now();

  @Column(name = "accumulated_pause_seconds", nullable = false)
  private int accumulatedPauseSeconds = 0;

  @Column(name = "paused_at")
  private Instant pausedAt;

  @Column(name = "completed_at")
  private Instant completedAt;

  @Column(name = "abandoned_at")
  private Instant abandonedAt;

  @Column(name = "duration_seconds")
  private Integer durationSeconds;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SessionStatus status = SessionStatus.ACTIVE;

  protected Session() {
  }

  public Session(
      UUID userId,
      SessionType sessionType,
      Integer plannedDurationSeconds,
      UUID goalId,
      Integer workMinutes,
      Integer breakMinutes,
      Integer cycleCount) {
    this.userId = userId;
    this.sessionType = sessionType;
    this.plannedDurationSeconds = plannedDurationSeconds;
    this.goalId = goalId;
    this.workMinutes = workMinutes;
    this.breakMinutes = breakMinutes;
    this.cycleCount = cycleCount;
  }

  public UUID getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public SessionType getSessionType() {
    return sessionType;
  }

  public Integer getPlannedDurationSeconds() {
    return plannedDurationSeconds;
  }

  public UUID getGoalId() {
    return goalId;
  }

  public Integer getWorkMinutes() {
    return workMinutes;
  }

  public Integer getBreakMinutes() {
    return breakMinutes;
  }

  public Integer getCycleCount() {
    return cycleCount;
  }

  public Instant getStartedAt() {
    return startedAt;
  }

  public int getAccumulatedPauseSeconds() {
    return accumulatedPauseSeconds;
  }

  public void setAccumulatedPauseSeconds(int accumulatedPauseSeconds) {
    this.accumulatedPauseSeconds = accumulatedPauseSeconds;
  }

  public Instant getPausedAt() {
    return pausedAt;
  }

  public void setPausedAt(Instant pausedAt) {
    this.pausedAt = pausedAt;
  }

  public Instant getCompletedAt() {
    return completedAt;
  }

  public void setCompletedAt(Instant completedAt) {
    this.completedAt = completedAt;
  }

  public Instant getAbandonedAt() {
    return abandonedAt;
  }

  public void setAbandonedAt(Instant abandonedAt) {
    this.abandonedAt = abandonedAt;
  }

  public Integer getDurationSeconds() {
    return durationSeconds;
  }

  public void setDurationSeconds(Integer durationSeconds) {
    this.durationSeconds = durationSeconds;
  }

  public SessionStatus getStatus() {
    return status;
  }

  public void setStatus(SessionStatus status) {
    this.status = status;
  }

  public boolean isTerminal() {
    return status == SessionStatus.COMPLETED || status == SessionStatus.ABANDONED;
  }
}

package com.locus.analytics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

// Per-user, per-calendar-day rollup, per frd.md's Analytics data model.
@Entity
@Table(name = "session_stats_daily")
@IdClass(SessionStatsDaily.Key.class)
public class SessionStatsDaily {

  @Id
  @Column(name = "user_id")
  private UUID userId;

  @Id
  @Column(name = "stat_date")
  private LocalDate statDate;

  @Column(name = "sessions_completed", nullable = false)
  private int sessionsCompleted;

  @Column(name = "sessions_abandoned", nullable = false)
  private int sessionsAbandoned;

  @Column(name = "total_focus_seconds", nullable = false)
  private long totalFocusSeconds;

  @Column(name = "has_qualifying_session", nullable = false)
  private boolean hasQualifyingSession;

  @Column(name = "distraction_count", nullable = false)
  private int distractionCount;

  protected SessionStatsDaily() {
  }

  public SessionStatsDaily(UUID userId, LocalDate statDate) {
    this.userId = userId;
    this.statDate = statDate;
  }

  public UUID getUserId() {
    return userId;
  }

  public LocalDate getStatDate() {
    return statDate;
  }

  public int getSessionsCompleted() {
    return sessionsCompleted;
  }

  public int getSessionsAbandoned() {
    return sessionsAbandoned;
  }

  public long getTotalFocusSeconds() {
    return totalFocusSeconds;
  }

  public boolean isHasQualifyingSession() {
    return hasQualifyingSession;
  }

  public void recordCompletedSession(long durationSeconds, boolean qualifies) {
    this.sessionsCompleted += 1;
    this.totalFocusSeconds += durationSeconds;
    this.hasQualifyingSession = this.hasQualifyingSession || qualifies;
  }

  public void recordAbandonedSession() {
    this.sessionsAbandoned += 1;
  }

  public int getDistractionCount() {
    return distractionCount;
  }

  public void recordDistraction() {
    this.distractionCount += 1;
  }

  public static class Key implements Serializable {
    private UUID userId;
    private LocalDate statDate;

    public Key() {
    }

    public Key(UUID userId, LocalDate statDate) {
      this.userId = userId;
      this.statDate = statDate;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Key key)) {
        return false;
      }
      return Objects.equals(userId, key.userId) && Objects.equals(statDate, key.statDate);
    }

    @Override
    public int hashCode() {
      return Objects.hash(userId, statDate);
    }
  }
}

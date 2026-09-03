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

// Per-user, per-day, per-hour-of-day focus-seconds — feeds the rolling-90-day best-study-hours
// metric (see V1__init.sql for why this table exists beyond frd.md's literal 4-table list).
@Entity
@Table(name = "session_stats_hourly")
@IdClass(SessionStatsHourly.Key.class)
public class SessionStatsHourly {

  @Id
  @Column(name = "user_id")
  private UUID userId;

  @Id
  @Column(name = "stat_date")
  private LocalDate statDate;

  @Id
  @Column(name = "hour_of_day")
  private short hourOfDay;

  @Column(name = "focus_seconds", nullable = false)
  private long focusSeconds;

  protected SessionStatsHourly() {
  }

  public SessionStatsHourly(UUID userId, LocalDate statDate, int hourOfDay) {
    this.userId = userId;
    this.statDate = statDate;
    this.hourOfDay = (short) hourOfDay;
  }

  public UUID getUserId() {
    return userId;
  }

  public LocalDate getStatDate() {
    return statDate;
  }

  public int getHourOfDay() {
    return hourOfDay;
  }

  public long getFocusSeconds() {
    return focusSeconds;
  }

  public void addFocusSeconds(long durationSeconds) {
    this.focusSeconds += durationSeconds;
  }

  public static class Key implements Serializable {
    private UUID userId;
    private LocalDate statDate;
    private short hourOfDay;

    public Key() {
    }

    public Key(UUID userId, LocalDate statDate, int hourOfDay) {
      this.userId = userId;
      this.statDate = statDate;
      this.hourOfDay = (short) hourOfDay;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Key key)) {
        return false;
      }
      return hourOfDay == key.hourOfDay && Objects.equals(userId, key.userId) && Objects.equals(statDate, key.statDate);
    }

    @Override
    public int hashCode() {
      return Objects.hash(userId, statDate, hourOfDay);
    }
  }
}

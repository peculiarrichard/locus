package com.locus.analytics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

// All-time (not date-windowed), per frd.md's literal Analytics data model.
@Entity
@Table(name = "distraction_stats_hourly")
@IdClass(DistractionStatsHourly.Key.class)
public class DistractionStatsHourly {

  @Id
  @Column(name = "user_id")
  private UUID userId;

  @Id
  @Column(name = "hour_of_day")
  private short hourOfDay;

  @Column(name = "distraction_count", nullable = false)
  private int distractionCount;

  @Column(name = "total_focus_seconds_in_bucket", nullable = false)
  private long totalFocusSecondsInBucket;

  protected DistractionStatsHourly() {
  }

  public DistractionStatsHourly(UUID userId, int hourOfDay) {
    this.userId = userId;
    this.hourOfDay = (short) hourOfDay;
  }

  public UUID getUserId() {
    return userId;
  }

  public int getHourOfDay() {
    return hourOfDay;
  }

  public int getDistractionCount() {
    return distractionCount;
  }

  public long getTotalFocusSecondsInBucket() {
    return totalFocusSecondsInBucket;
  }

  public void addDistraction() {
    this.distractionCount += 1;
  }

  public void addFocusSeconds(long durationSeconds) {
    this.totalFocusSecondsInBucket += durationSeconds;
  }

  public static class Key implements Serializable {
    private UUID userId;
    private short hourOfDay;

    public Key() {
    }

    public Key(UUID userId, int hourOfDay) {
      this.userId = userId;
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
      return hourOfDay == key.hourOfDay && Objects.equals(userId, key.userId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(userId, hourOfDay);
    }
  }
}

package com.locus.analytics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "streaks")
public class Streak {

  @Id
  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "current_streak_days", nullable = false)
  private int currentStreakDays;

  @Column(name = "longest_streak_days", nullable = false)
  private int longestStreakDays;

  @Column(name = "last_qualifying_day")
  private LocalDate lastQualifyingDay;

  protected Streak() {
  }

  public Streak(UUID userId) {
    this.userId = userId;
  }

  public UUID getUserId() {
    return userId;
  }

  public int getCurrentStreakDays() {
    return currentStreakDays;
  }

  public int getLongestStreakDays() {
    return longestStreakDays;
  }

  public LocalDate getLastQualifyingDay() {
    return lastQualifyingDay;
  }

  public void extend(LocalDate qualifyingDay) {
    this.currentStreakDays += 1;
    this.longestStreakDays = Math.max(this.longestStreakDays, this.currentStreakDays);
    this.lastQualifyingDay = qualifyingDay;
  }

  public int reset() {
    int before = this.currentStreakDays;
    this.currentStreakDays = 0;
    return before;
  }
}

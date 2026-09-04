package com.locus.analytics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

// Own local copy of a user's timezone, kept current via UserProfileUpdated, per frd.md — used for
// streak day-boundary and hour-of-day bucketing so this service doesn't need a synchronous call
// to Auth Service.
@Entity
@Table(name = "user_locale")
public class UserLocale {

  @Id
  @Column(name = "user_id")
  private UUID userId;

  @Column(nullable = false)
  private String timezone;

  protected UserLocale() {
  }

  public UserLocale(UUID userId, String timezone) {
    this.userId = userId;
    this.timezone = timezone;
  }

  public UUID getUserId() {
    return userId;
  }

  public String getTimezone() {
    return timezone;
  }

  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }
}

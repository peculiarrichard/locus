package com.locus.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

// The T1 PII store, per frd.md/technical-spec.md §9.
@Entity
@Table(name = "user_contacts")
public class UserContact {

  @Id
  @Column(name = "user_id")
  private UUID userId;

  @Column(nullable = false)
  private String email;

  @Column(name = "display_name")
  private String displayName;

  @Column(nullable = false)
  private String timezone;

  @Column(name = "reminder_time")
  private LocalTime reminderTime;

  @Column(nullable = false)
  private boolean bounced;

  @Column(name = "last_reminder_sent_date")
  private LocalDate lastReminderSentDate;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();

  protected UserContact() {
  }

  public UserContact(UUID userId, String email, String displayName, String timezone) {
    this.userId = userId;
    this.email = email;
    this.displayName = displayName;
    this.timezone = timezone;
  }

  public UUID getUserId() {
    return userId;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getTimezone() {
    return timezone;
  }

  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }

  public LocalTime getReminderTime() {
    return reminderTime;
  }

  public void setReminderTime(LocalTime reminderTime) {
    this.reminderTime = reminderTime;
  }

  public boolean isBounced() {
    return bounced;
  }

  public void setBounced(boolean bounced) {
    this.bounced = bounced;
  }

  public LocalDate getLastReminderSentDate() {
    return lastReminderSentDate;
  }

  public void setLastReminderSentDate(LocalDate lastReminderSentDate) {
    this.lastReminderSentDate = lastReminderSentDate;
  }

  public void touch() {
    this.updatedAt = Instant.now();
  }
}

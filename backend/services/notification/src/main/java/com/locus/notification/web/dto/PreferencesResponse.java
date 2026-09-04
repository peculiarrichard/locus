package com.locus.notification.web.dto;

import com.locus.notification.domain.UserContact;
import java.time.LocalTime;

public record PreferencesResponse(String email, String timezone, LocalTime reminderTime) {

  public static PreferencesResponse from(UserContact contact) {
    return new PreferencesResponse(contact.getEmail(), contact.getTimezone(), contact.getReminderTime());
  }
}

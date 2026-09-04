package com.locus.notification.web.dto;

import java.time.LocalTime;

// Null reminderTime opts out of the daily study reminder.
public record UpdatePreferencesRequest(LocalTime reminderTime) {
}

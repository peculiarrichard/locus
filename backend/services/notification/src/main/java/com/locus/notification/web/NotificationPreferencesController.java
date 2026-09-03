package com.locus.notification.web;

import com.locus.notification.exception.ApiException;
import com.locus.notification.service.NotificationService;
import com.locus.notification.web.dto.PreferencesResponse;
import com.locus.notification.web.dto.UpdatePreferencesRequest;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

// Self-service reminder preferences, per frd.md's Notification Service API surface.
@RestController
public class NotificationPreferencesController {

  private final NotificationService notificationService;

  public NotificationPreferencesController(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @GetMapping("/notifications/preferences")
  public PreferencesResponse get(@AuthenticationPrincipal Jwt jwt) {
    return notificationService
        .getPreferences(UUID.fromString(jwt.getSubject()))
        .map(PreferencesResponse::from)
        .orElseThrow(() -> ApiException.notFound("Contact record"));
  }

  @PatchMapping("/notifications/preferences")
  public PreferencesResponse update(@AuthenticationPrincipal Jwt jwt, @RequestBody UpdatePreferencesRequest request) {
    return PreferencesResponse.from(
        notificationService.updatePreferences(UUID.fromString(jwt.getSubject()), request.reminderTime()));
  }
}

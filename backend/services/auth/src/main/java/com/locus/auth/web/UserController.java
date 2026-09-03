package com.locus.auth.web;

import com.locus.auth.service.UserProfileService;
import com.locus.auth.web.dto.DeviceResponse;
import com.locus.auth.web.dto.ProfileResponse;
import com.locus.auth.web.dto.ProfileUpdateRequest;
import com.locus.auth.web.dto.ReauthRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// Self-service profile, device (session) management, and account deletion, per frd.md.
@RestController
public class UserController {

  private final UserProfileService userProfileService;

  public UserController(UserProfileService userProfileService) {
    this.userProfileService = userProfileService;
  }

  @GetMapping("/users/me")
  public ProfileResponse getProfile(@AuthenticationPrincipal Jwt jwt) {
    return userProfileService.getProfile(UUID.fromString(jwt.getSubject()));
  }

  @PatchMapping("/users/me")
  public void updateProfile(@AuthenticationPrincipal Jwt jwt, @RequestBody ProfileUpdateRequest request) {
    userProfileService.updateProfile(UUID.fromString(jwt.getSubject()), request.displayName(), request.timezone());
  }

  @DeleteMapping("/users/me")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteAccount(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ReauthRequest request) {
    userProfileService.deleteAccount(UUID.fromString(jwt.getSubject()), request.password());
  }

  @GetMapping("/users/me/devices")
  public List<DeviceResponse> listDevices(@AuthenticationPrincipal Jwt jwt) {
    return userProfileService.listDevices(UUID.fromString(jwt.getSubject()));
  }

  @DeleteMapping("/users/me/devices/{deviceId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void revokeDevice(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID deviceId) {
    userProfileService.revokeDevice(UUID.fromString(jwt.getSubject()), deviceId);
  }
}

package com.locus.auth.web;

import com.locus.auth.service.AdminService;
import com.locus.auth.web.dto.AdminActionRequest;
import com.locus.auth.web.dto.AdminUserStatusResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// Admin-only account-status/unlock/revoke endpoints, ROLE_ADMIN-gated by SecurityConfig, per frd.md.
@RestController
public class AdminController {

  private final AdminService adminService;

  public AdminController(AdminService adminService) {
    this.adminService = adminService;
  }

  @GetMapping("/admin/users/{userId}")
  public AdminUserStatusResponse viewStatus(@PathVariable UUID userId) {
    return adminService.viewStatus(userId);
  }

  @PostMapping("/admin/users/{userId}/unlock")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void unlock(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID userId,
      @Valid @RequestBody AdminActionRequest request) {
    adminService.unlock(UUID.fromString(jwt.getSubject()), userId, request.reason());
  }

  @PostMapping("/admin/users/{userId}/revoke-tokens")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void revokeTokens(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID userId,
      @Valid @RequestBody AdminActionRequest request) {
    adminService.revokeAllTokens(UUID.fromString(jwt.getSubject()), userId, request.reason());
  }
}

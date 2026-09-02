package com.locus.auth.web;

import com.locus.auth.service.PasswordResetService;
import com.locus.auth.web.dto.PasswordResetConfirmRequest;
import com.locus.auth.web.dto.PasswordResetRequestRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// Public password-reset request/confirm endpoints, per frd.md.
@RestController
public class PasswordResetController {

  private final PasswordResetService passwordResetService;

  public PasswordResetController(PasswordResetService passwordResetService) {
    this.passwordResetService = passwordResetService;
  }

  @PostMapping("/auth/password-reset/request")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void requestReset(@Valid @RequestBody PasswordResetRequestRequest request) {
    passwordResetService.requestReset(request.email());
  }

  @PostMapping("/auth/password-reset/confirm")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void confirmReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
    passwordResetService.confirmReset(request.token(), request.newPassword());
  }
}

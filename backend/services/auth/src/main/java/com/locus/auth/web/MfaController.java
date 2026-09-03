package com.locus.auth.web;

import com.locus.auth.service.MfaService;
import com.locus.auth.web.dto.MfaConfirmRequest;
import com.locus.auth.web.dto.MfaConfirmResponse;
import com.locus.auth.web.dto.MfaEnrollResponse;
import com.locus.auth.web.dto.ReauthRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// Authenticated MFA enroll/confirm/disable endpoints, per frd.md.
@RestController
public class MfaController {

  private final MfaService mfaService;

  public MfaController(MfaService mfaService) {
    this.mfaService = mfaService;
  }

  @PostMapping("/auth/mfa/enroll")
  public MfaEnrollResponse enroll(@AuthenticationPrincipal Jwt jwt) {
    String uri = mfaService.enroll(UUID.fromString(jwt.getSubject()));
    return new MfaEnrollResponse(uri);
  }

  @PostMapping("/auth/mfa/confirm")
  public MfaConfirmResponse confirm(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody MfaConfirmRequest request) {
    var codes = mfaService.confirm(UUID.fromString(jwt.getSubject()), request.code());
    return new MfaConfirmResponse(codes);
  }

  @PostMapping("/auth/mfa/disable")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void disable(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ReauthRequest request) {
    mfaService.disable(UUID.fromString(jwt.getSubject()), request.password());
  }
}

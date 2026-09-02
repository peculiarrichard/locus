package com.locus.auth.web;

import com.locus.auth.exception.ApiException;
import com.locus.auth.security.JwtService;
import com.locus.auth.service.LoginService;
import com.locus.auth.service.MfaService;
import com.locus.auth.service.RegistrationService;
import com.locus.auth.web.dto.LoginRequest;
import com.locus.auth.web.dto.LoginResponse;
import com.locus.auth.web.dto.MfaChallengeVerifyRequest;
import com.locus.auth.web.dto.RefreshRequest;
import com.locus.auth.web.dto.RegisterRequest;
import com.locus.auth.web.dto.ResendVerificationRequest;
import com.locus.auth.web.dto.TokenResponse;
import com.locus.auth.web.dto.VerifyEmailRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// Registration, email verification, login, MFA challenge, refresh, and logout endpoints, per frd.md.
@RestController
public class AuthController {

  private final RegistrationService registrationService;
  private final LoginService loginService;
  private final MfaService mfaService;
  private final JwtService jwtService;

  public AuthController(RegistrationService registrationService, LoginService loginService, MfaService mfaService,
      JwtService jwtService) {
    this.registrationService = registrationService;
    this.loginService = loginService;
    this.mfaService = mfaService;
    this.jwtService = jwtService;
  }

  @PostMapping("/auth/register")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void register(@Valid @RequestBody RegisterRequest request) {
    registrationService.register(request.email(), request.password());
  }

  @PostMapping("/auth/verify-email/resend")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
    registrationService.resendVerification(request.email());
  }

  @PostMapping("/auth/verify-email")
  public void verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
    registrationService.verifyEmail(request.token());
  }

  @PostMapping("/auth/login")
  public LoginResponse login(@Valid @RequestBody LoginRequest request) {
    return loginService.login(request.email(), request.password(), request.deviceLabel());
  }

  @PostMapping("/auth/mfa/challenge")
  public TokenResponse mfaChallenge(@Valid @RequestBody MfaChallengeVerifyRequest request) {
    UUID userId = jwtService.verifyMfaChallengeToken(request.mfaChallengeToken());
    if (!mfaService.verifyCodeOrRecoveryCode(userId, request.code())) {
      throw ApiException.invalidToken("MFA code");
    }
    return loginService.completeMfaLogin(userId, null);
  }

  @PostMapping("/auth/refresh")
  public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
    return loginService.refresh(request.refreshToken());
  }

  @PostMapping("/auth/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logout(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody RefreshRequest request) {
    loginService.logout(UUID.fromString(jwt.getSubject()), request.refreshToken());
  }
}

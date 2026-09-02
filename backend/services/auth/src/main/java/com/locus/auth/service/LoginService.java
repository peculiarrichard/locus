package com.locus.auth.service;

import com.locus.auth.domain.MfaSecret;
import com.locus.auth.domain.RefreshToken;
import com.locus.auth.domain.User;
import com.locus.auth.exception.ApiException;
import com.locus.auth.repository.MfaSecretRepository;
import com.locus.auth.repository.RefreshTokenRepository;
import com.locus.auth.repository.UserRepository;
import com.locus.auth.security.JwtService;
import com.locus.auth.security.RandomTokenGenerator;
import com.locus.auth.security.TokenHasher;
import com.locus.auth.web.dto.LoginResponse;
import com.locus.auth.web.dto.TokenResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Login, token refresh, and logout, including the lockout policy and refresh-token
// rotation/theft-detection, per frd.md + technical-spec.md §1.
@Service
public class LoginService {

  private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(30);

  private final UserRepository userRepository;
  private final MfaSecretRepository mfaSecretRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final TokenHasher tokenHasher;
  private final RandomTokenGenerator tokenGenerator;
  private final TokenFamilyRevocationService tokenFamilyRevocationService;

  @Value("${locus.lockout.max-attempts}")
  private int maxAttempts;

  @Value("${locus.lockout.window-minutes}")
  private long windowMinutes;

  @Value("${locus.lockout.lock-minutes}")
  private long lockMinutes;

  public LoginService(UserRepository userRepository, MfaSecretRepository mfaSecretRepository,
      RefreshTokenRepository refreshTokenRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
      TokenHasher tokenHasher, RandomTokenGenerator tokenGenerator,
      TokenFamilyRevocationService tokenFamilyRevocationService) {
    this.userRepository = userRepository;
    this.mfaSecretRepository = mfaSecretRepository;
    this.refreshTokenRepository = refreshTokenRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.tokenHasher = tokenHasher;
    this.tokenGenerator = tokenGenerator;
    this.tokenFamilyRevocationService = tokenFamilyRevocationService;
  }

  @Transactional
  public LoginResponse login(String email, String rawPassword, String deviceLabel) {
    User user = userRepository.findByEmail(email).orElseThrow(ApiException::invalidCredentials);

    if (user.isLocked()) {
      throw ApiException.accountLocked(user.getLockedUntil());
    }
    if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
      recordFailedAttempt(user);
      throw ApiException.invalidCredentials();
    }
    if (!user.isEmailVerified()) {
      throw ApiException.emailNotVerified();
    }

    user.setFailedLoginCount(0);
    user.setLastFailedLoginAt(null);
    userRepository.save(user);

    boolean mfaEnabled = mfaSecretRepository.findById(user.getId()).map(MfaSecret::isConfirmed).orElse(false);
    if (mfaEnabled) {
      return LoginResponse.mfaRequired(jwtService.issueMfaChallengeToken(user.getId()));
    }
    String refreshToken = issueRefreshToken(user, deviceLabel, UUID.randomUUID());
    return LoginResponse.tokens(jwtService.issueAccessToken(user, false), refreshToken);
  }

  // Called after a caller has already verified an MFA code against the challenge
  // token.
  @Transactional
  public TokenResponse completeMfaLogin(UUID userId, String deviceLabel) {
    User user = userRepository.findById(userId).orElseThrow(ApiException::invalidCredentials);
    String refreshToken = issueRefreshToken(user, deviceLabel, UUID.randomUUID());
    return new TokenResponse(jwtService.issueAccessToken(user, true), refreshToken);
  }

  @Transactional
  public TokenResponse refresh(String rawRefreshToken) {
    String hash = tokenHasher.hash(rawRefreshToken);
    RefreshToken token = refreshTokenRepository.findByTokenHash(hash)
        .orElseThrow(() -> ApiException.invalidToken("refresh token"));

    if (token.getRevokedAt() != null) {
      // Reuse of an already-rotated-out token is a theft signal: revoke the entire
      // family. This
      // must commit even though we're about to throw and roll back this method's own
      // transaction,
      // hence the separate REQUIRES_NEW-transactional collaborator rather than a
      // local call.
      tokenFamilyRevocationService.revokeFamily(token.getFamilyId());
      throw ApiException.invalidToken("refresh token");
    }
    if (!token.isActive()) {
      throw ApiException.invalidToken("refresh token");
    }

    User user = userRepository.findById(token.getUserId()).orElseThrow(ApiException::invalidCredentials);
    token.setRevokedAt(Instant.now());
    refreshTokenRepository.save(token);
    String newRefreshToken = issueRefreshToken(user, token.getDeviceLabel(), token.getFamilyId());
    return new TokenResponse(jwtService.issueAccessToken(user, false), newRefreshToken);
  }

  @Transactional
  public void logout(UUID userId, String rawRefreshToken) {
    String hash = tokenHasher.hash(rawRefreshToken);
    refreshTokenRepository.findByTokenHash(hash).filter(t -> t.getUserId().equals(userId)).ifPresent(t -> {
      t.setRevokedAt(Instant.now());
      refreshTokenRepository.save(t);
    });
  }

  private void recordFailedAttempt(User user) {
    Instant now = Instant.now();
    boolean withinWindow = user.getLastFailedLoginAt() != null
        && user.getLastFailedLoginAt().isAfter(now.minus(Duration.ofMinutes(windowMinutes)));
    user.setFailedLoginCount(withinWindow ? user.getFailedLoginCount() + 1 : 1);
    user.setLastFailedLoginAt(now);
    if (user.getFailedLoginCount() >= maxAttempts) {
      user.setLockedUntil(now.plus(Duration.ofMinutes(lockMinutes)));
    }
    userRepository.save(user);
  }

  private String issueRefreshToken(User user, String deviceLabel, UUID familyId) {
    String rawToken = tokenGenerator.generate();
    RefreshToken refreshToken = new RefreshToken(user.getId(), tokenHasher.hash(rawToken), deviceLabel, familyId,
        Instant.now().plus(REFRESH_TOKEN_TTL));
    refreshTokenRepository.save(refreshToken);
    return rawToken;
  }
}

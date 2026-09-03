package com.locus.auth.service;

import com.locus.auth.domain.PasswordResetToken;
import com.locus.auth.domain.User;
import com.locus.auth.event.EventPublisher;
import com.locus.auth.event.PasswordResetRequestedPayload;
import com.locus.auth.exception.ApiException;
import com.locus.auth.repository.PasswordResetTokenRepository;
import com.locus.auth.repository.RefreshTokenRepository;
import com.locus.auth.repository.UserRepository;
import com.locus.auth.security.PasswordPolicy;
import com.locus.auth.security.RandomTokenGenerator;
import com.locus.auth.security.TokenHasher;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Password reset request/confirm, per frd.md's Auth & User Service section.
@Service
public class PasswordResetService {

  private final UserRepository userRepository;
  private final PasswordResetTokenRepository resetTokenRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final PasswordPolicy passwordPolicy;
  private final EventPublisher eventPublisher;
  private final TokenHasher tokenHasher;
  private final RandomTokenGenerator tokenGenerator;

  @Value("${locus.password-reset.token-ttl-hours}")
  private long tokenTtlHours;

  public PasswordResetService(UserRepository userRepository, PasswordResetTokenRepository resetTokenRepository,
      RefreshTokenRepository refreshTokenRepository, PasswordEncoder passwordEncoder, PasswordPolicy passwordPolicy,
      EventPublisher eventPublisher, TokenHasher tokenHasher, RandomTokenGenerator tokenGenerator) {
    this.userRepository = userRepository;
    this.resetTokenRepository = resetTokenRepository;
    this.refreshTokenRepository = refreshTokenRepository;
    this.passwordEncoder = passwordEncoder;
    this.passwordPolicy = passwordPolicy;
    this.eventPublisher = eventPublisher;
    this.tokenHasher = tokenHasher;
    this.tokenGenerator = tokenGenerator;
  }

  @Transactional
  public void requestReset(String email) {
    // Always returns quietly for a nonexistent email — doesn't leak account
    // existence.
    userRepository.findByEmail(email).ifPresent(this::issueResetToken);
  }

  @Transactional
  public void confirmReset(String rawToken, String newPassword) {
    if (!passwordPolicy.isValid(newPassword)) {
      throw ApiException.weakPassword();
    }
    String hash = tokenHasher.hash(rawToken);
    PasswordResetToken token = resetTokenRepository.findByTokenHash(hash).filter(PasswordResetToken::isValid)
        .orElseThrow(() -> ApiException.invalidToken("password reset token"));
    User user = userRepository.findById(token.getUserId()).orElseThrow(() -> ApiException.notFound("User"));

    user.setPasswordHash(passwordEncoder.encode(newPassword));
    userRepository.save(user);
    token.setUsedAt(Instant.now());
    resetTokenRepository.save(token);

    // A password reset is itself a signal the old credential may have been
    // compromised — force logout everywhere.
    Instant now = Instant.now();
    refreshTokenRepository.findByUserIdAndRevokedAtIsNull(user.getId()).forEach(rt -> {
      rt.setRevokedAt(now);
      refreshTokenRepository.save(rt);
    });
  }

  private void issueResetToken(User user) {
    String rawToken = tokenGenerator.generate();
    resetTokenRepository.save(new PasswordResetToken(user.getId(), tokenHasher.hash(rawToken),
        Instant.now().plus(Duration.ofHours(tokenTtlHours))));
    eventPublisher
        .publishPasswordResetRequested(new PasswordResetRequestedPayload(user.getId(), user.getEmail(), rawToken));
  }
}

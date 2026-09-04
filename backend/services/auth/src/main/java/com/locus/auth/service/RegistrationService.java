package com.locus.auth.service;

import com.locus.auth.domain.EmailVerificationToken;
import com.locus.auth.domain.Role;
import com.locus.auth.domain.User;
import com.locus.auth.event.EventPublisher;
import com.locus.auth.event.UserRegisteredPayload;
import com.locus.auth.exception.ApiException;
import com.locus.auth.repository.EmailVerificationTokenRepository;
import com.locus.auth.repository.RoleRepository;
import com.locus.auth.repository.UserRepository;
import com.locus.auth.security.PasswordPolicy;
import com.locus.auth.security.RandomTokenGenerator;
import com.locus.auth.security.TokenHasher;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Registration and email-verification flow, per frd.md's Auth & User Service section.
@Service
public class RegistrationService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final EmailVerificationTokenRepository verificationTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final PasswordPolicy passwordPolicy;
  private final EventPublisher eventPublisher;
  private final TokenHasher tokenHasher;
  private final RandomTokenGenerator tokenGenerator;

  @Value("${locus.email-verification.token-ttl-hours}")
  private long verificationTtlHours;

  @Value("${locus.email-verification.resend-cooldown-seconds}")
  private long resendCooldownSeconds;

  public RegistrationService(UserRepository userRepository, RoleRepository roleRepository,
      EmailVerificationTokenRepository verificationTokenRepository, PasswordEncoder passwordEncoder,
      PasswordPolicy passwordPolicy, EventPublisher eventPublisher, TokenHasher tokenHasher,
      RandomTokenGenerator tokenGenerator) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.verificationTokenRepository = verificationTokenRepository;
    this.passwordEncoder = passwordEncoder;
    this.passwordPolicy = passwordPolicy;
    this.eventPublisher = eventPublisher;
    this.tokenHasher = tokenHasher;
    this.tokenGenerator = tokenGenerator;
  }

  @Transactional
  public void register(String email, String rawPassword) {
    if (!passwordPolicy.isValid(rawPassword)) {
      throw ApiException.weakPassword();
    }
    var existing = userRepository.findByEmail(email);
    if (existing.isPresent()) {
      User user = existing.get();
      if (user.isEmailVerified()) {
        throw ApiException.emailAlreadyRegistered();
      }
      // Unverified duplicate: resend verification instead of leaking "email taken",
      // per frd.md's edge case.
      issueVerificationToken(user);
      return;
    }
    Role userRole = roleRepository.findByName("user")
        .orElseThrow(() -> new IllegalStateException("Seed role 'user' is missing"));
    User user = new User(email, passwordEncoder.encode(rawPassword));
    user.setRoles(Set.of(userRole));
    userRepository.save(user);
    issueVerificationToken(user);
  }

  @Transactional
  public void resendVerification(String email) {
    // Always returns quietly for a nonexistent or already-verified email — doesn't
    // leak account existence.
    userRepository.findByEmail(email).filter(u -> !u.isEmailVerified()).ifPresent(this::issueVerificationToken);
  }

  @Transactional
  public void verifyEmail(String rawToken) {
    String hash = tokenHasher.hash(rawToken);
    EmailVerificationToken token = verificationTokenRepository.findByTokenHash(hash)
        .filter(EmailVerificationToken::isValid).orElseThrow(() -> ApiException.invalidToken("verification token"));
    User user = userRepository.findById(token.getUserId()).orElseThrow(() -> ApiException.notFound("User"));
    user.setEmailVerified(true);
    token.setUsedAt(Instant.now());
    userRepository.save(user);
    verificationTokenRepository.save(token);
  }

  private void issueVerificationToken(User user) {
    if (verificationTokenRepository.recentlySent(user.getId(), Instant.now().minusSeconds(resendCooldownSeconds))) {
      throw ApiException.rateLimited("Verification email");
    }
    String rawToken = tokenGenerator.generate();
    verificationTokenRepository.save(new EmailVerificationToken(user.getId(), tokenHasher.hash(rawToken),
        Instant.now().plus(Duration.ofHours(verificationTtlHours))));
    eventPublisher.publishUserRegistered(
        new UserRegisteredPayload(user.getId(), user.getEmail(), user.getDisplayName(), rawToken));
  }
}

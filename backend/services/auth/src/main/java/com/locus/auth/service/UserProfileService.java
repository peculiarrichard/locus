package com.locus.auth.service;

import com.locus.auth.domain.MfaSecret;
import com.locus.auth.domain.RefreshToken;
import com.locus.auth.domain.User;
import com.locus.auth.domain.UserStatus;
import com.locus.auth.event.EventPublisher;
import com.locus.auth.event.UserDeletedPayload;
import com.locus.auth.event.UserProfileUpdatedPayload;
import com.locus.auth.exception.ApiException;
import com.locus.auth.repository.MfaSecretRepository;
import com.locus.auth.repository.RefreshTokenRepository;
import com.locus.auth.repository.UserRepository;
import com.locus.auth.web.dto.DeviceResponse;
import com.locus.auth.web.dto.ProfileResponse;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Self-service profile, device (refresh-token session) management, and account deletion, per frd.md.
@Service
public class UserProfileService {

  private final UserRepository userRepository;
  private final MfaSecretRepository mfaSecretRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final EventPublisher eventPublisher;

  public UserProfileService(UserRepository userRepository, MfaSecretRepository mfaSecretRepository,
      RefreshTokenRepository refreshTokenRepository, PasswordEncoder passwordEncoder, EventPublisher eventPublisher) {
    this.userRepository = userRepository;
    this.mfaSecretRepository = mfaSecretRepository;
    this.refreshTokenRepository = refreshTokenRepository;
    this.passwordEncoder = passwordEncoder;
    this.eventPublisher = eventPublisher;
  }

  @Transactional(readOnly = true)
  public ProfileResponse getProfile(UUID userId) {
    User user = userRepository.findById(userId).orElseThrow(() -> ApiException.notFound("User"));
    boolean mfaEnabled = mfaSecretRepository.findById(userId).map(MfaSecret::isConfirmed).orElse(false);
    return new ProfileResponse(user.getId(), user.getEmail(), user.getDisplayName(), user.getTimezone(),
        user.isEmailVerified(), mfaEnabled);
  }

  @Transactional
  public void updateProfile(UUID userId, String displayName, String timezone) {
    User user = userRepository.findById(userId).orElseThrow(() -> ApiException.notFound("User"));
    if (displayName != null) {
      user.setDisplayName(displayName);
    }
    if (timezone != null) {
      user.setTimezone(timezone);
    }
    userRepository.save(user);
    eventPublisher.publishUserProfileUpdated(
        new UserProfileUpdatedPayload(user.getId(), user.getEmail(), user.getDisplayName(), user.getTimezone()));
  }

  @Transactional
  public void deleteAccount(UUID userId, String password) {
    User user = userRepository.findById(userId).orElseThrow(() -> ApiException.notFound("User"));
    if (!passwordEncoder.matches(password, user.getPasswordHash())) {
      throw ApiException.invalidCredentials();
    }
    Instant now = Instant.now();
    // admin_audit_log rows referencing this user must survive (technical-spec.md
    // §9's exemption), so
    // the users row is anonymized in place rather than hard-deleted; MFA and active
    // sessions are cleared.
    user.setEmail("deleted-" + userId + "@locus.invalid");
    user.setPasswordHash("!");
    user.setDisplayName(null);
    user.setTimezone(null);
    user.setStatus(UserStatus.DISABLED);
    user.setRoles(Set.of());
    userRepository.save(user);
    mfaSecretRepository.deleteById(userId);
    refreshTokenRepository.findByUserIdAndRevokedAtIsNull(userId).forEach(rt -> {
      rt.setRevokedAt(now);
      refreshTokenRepository.save(rt);
    });
    eventPublisher.publishUserDeleted(new UserDeletedPayload(userId, now));
  }

  @Transactional(readOnly = true)
  public List<DeviceResponse> listDevices(UUID userId) {
    return refreshTokenRepository.findByUserIdAndRevokedAtIsNull(userId).stream().filter(RefreshToken::isActive)
        .map(rt -> new DeviceResponse(rt.getId(), rt.getDeviceLabel(), rt.getCreatedAt(), rt.getExpiresAt())).toList();
  }

  @Transactional
  public void revokeDevice(UUID userId, UUID refreshTokenId) {
    RefreshToken token = refreshTokenRepository.findById(refreshTokenId).filter(rt -> rt.getUserId().equals(userId))
        .orElseThrow(() -> ApiException.notFound("Device"));
    token.setRevokedAt(Instant.now());
    refreshTokenRepository.save(token);
  }
}

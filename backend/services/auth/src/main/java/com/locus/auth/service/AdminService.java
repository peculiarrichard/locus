package com.locus.auth.service;

import com.locus.auth.domain.AdminAuditLog;
import com.locus.auth.domain.MfaSecret;
import com.locus.auth.domain.User;
import com.locus.auth.exception.ApiException;
import com.locus.auth.repository.AdminAuditLogRepository;
import com.locus.auth.repository.MfaSecretRepository;
import com.locus.auth.repository.RefreshTokenRepository;
import com.locus.auth.repository.UserRepository;
import com.locus.auth.web.dto.AdminUserStatusResponse;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Admin operations — view status, unlock, force-revoke tokens — every action audited, per frd.md/technical-spec.md §1.
@Service
public class AdminService {

  private final UserRepository userRepository;
  private final MfaSecretRepository mfaSecretRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final AdminAuditLogRepository auditLogRepository;

  public AdminService(UserRepository userRepository, MfaSecretRepository mfaSecretRepository,
      RefreshTokenRepository refreshTokenRepository, AdminAuditLogRepository auditLogRepository) {
    this.userRepository = userRepository;
    this.mfaSecretRepository = mfaSecretRepository;
    this.refreshTokenRepository = refreshTokenRepository;
    this.auditLogRepository = auditLogRepository;
  }

  @Transactional(readOnly = true)
  public AdminUserStatusResponse viewStatus(UUID targetUserId) {
    User user = userRepository.findById(targetUserId).orElseThrow(() -> ApiException.notFound("User"));
    boolean mfaEnabled = mfaSecretRepository.findById(targetUserId).map(MfaSecret::isConfirmed).orElse(false);
    return new AdminUserStatusResponse(user.getId(), user.getEmail(), user.getStatus().name(), user.isEmailVerified(),
        mfaEnabled, user.getFailedLoginCount(), user.getLockedUntil());
  }

  @Transactional
  public void unlock(UUID adminUserId, UUID targetUserId, String reason) {
    User user = userRepository.findById(targetUserId).orElseThrow(() -> ApiException.notFound("User"));
    user.setLockedUntil(null);
    user.setFailedLoginCount(0);
    user.setLastFailedLoginAt(null);
    userRepository.save(user);
    audit(adminUserId, targetUserId, "UNLOCK_ACCOUNT", reason);
  }

  @Transactional
  public void revokeAllTokens(UUID adminUserId, UUID targetUserId, String reason) {
    if (!userRepository.existsById(targetUserId)) {
      throw ApiException.notFound("User");
    }
    Instant now = Instant.now();
    refreshTokenRepository.findByUserIdAndRevokedAtIsNull(targetUserId).forEach(rt -> {
      rt.setRevokedAt(now);
      refreshTokenRepository.save(rt);
    });
    audit(adminUserId, targetUserId, "REVOKE_ALL_TOKENS", reason);
  }

  private void audit(UUID adminUserId, UUID targetUserId, String action, String reason) {
    auditLogRepository.save(new AdminAuditLog(adminUserId, targetUserId, action, reason));
  }
}

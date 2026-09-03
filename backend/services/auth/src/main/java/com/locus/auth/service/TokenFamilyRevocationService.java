package com.locus.auth.service;

import com.locus.auth.repository.RefreshTokenRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

// Revokes an entire refresh-token family on a theft signal, in its own transaction so the revocation
// commits even though the caller is about to throw and roll back its own transaction.
@Service
public class TokenFamilyRevocationService {

  private final RefreshTokenRepository refreshTokenRepository;

  public TokenFamilyRevocationService(RefreshTokenRepository refreshTokenRepository) {
    this.refreshTokenRepository = refreshTokenRepository;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void revokeFamily(UUID familyId) {
    Instant now = Instant.now();
    refreshTokenRepository.findByFamilyId(familyId).forEach(t -> {
      if (t.getRevokedAt() == null) {
        t.setRevokedAt(now);
        refreshTokenRepository.save(t);
      }
    });
  }
}

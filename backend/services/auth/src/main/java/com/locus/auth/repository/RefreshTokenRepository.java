package com.locus.auth.repository;

import com.locus.auth.domain.RefreshToken;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

// Spring Data repository for refresh_tokens.
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
  Optional<RefreshToken> findByTokenHash(String tokenHash);

  List<RefreshToken> findByUserIdAndRevokedAtIsNull(UUID userId);

  List<RefreshToken> findByFamilyId(UUID familyId);
}

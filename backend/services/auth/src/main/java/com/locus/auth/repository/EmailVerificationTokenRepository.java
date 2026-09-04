package com.locus.auth.repository;

import com.locus.auth.domain.EmailVerificationToken;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

// Spring Data repository for email_verification_tokens.
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {
  Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

  Optional<EmailVerificationToken> findFirstByUserIdAndUsedAtIsNullOrderByCreatedAtDesc(UUID userId);

  default boolean recentlySent(UUID userId, Instant since) {
    return findFirstByUserIdAndUsedAtIsNullOrderByCreatedAtDesc(userId).map(t -> t.getCreatedAt().isAfter(since))
        .orElse(false);
  }
}

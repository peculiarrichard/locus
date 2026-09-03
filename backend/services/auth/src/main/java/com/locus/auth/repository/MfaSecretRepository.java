package com.locus.auth.repository;

import com.locus.auth.domain.MfaSecret;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

// Spring Data repository for mfa_secrets.
public interface MfaSecretRepository extends JpaRepository<MfaSecret, UUID> {
  List<MfaSecret> findByConfirmedFalseAndCreatedAtBefore(Instant cutoff);
}

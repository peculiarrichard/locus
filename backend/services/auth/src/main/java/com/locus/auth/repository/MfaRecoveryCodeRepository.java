package com.locus.auth.repository;

import com.locus.auth.domain.MfaRecoveryCode;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

// Spring Data repository for mfa_recovery_codes.
public interface MfaRecoveryCodeRepository extends JpaRepository<MfaRecoveryCode, Long> {
  List<MfaRecoveryCode> findByUserIdAndUsedAtIsNull(UUID userId);
}

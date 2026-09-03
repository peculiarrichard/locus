package com.locus.auth.service;

import com.locus.auth.repository.MfaSecretRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// Purges MFA secrets left unconfirmed past their TTL, per frd.md's abandoned-enrollment edge case.
@Component
public class MfaEnrollmentCleanupJob {

  private final MfaSecretRepository mfaSecretRepository;

  @Value("${locus.mfa.enrollment-ttl-minutes}")
  private long enrollmentTtlMinutes;

  public MfaEnrollmentCleanupJob(MfaSecretRepository mfaSecretRepository) {
    this.mfaSecretRepository = mfaSecretRepository;
  }

  @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
  public void purgeAbandonedEnrollments() {
    Instant cutoff = Instant.now().minus(Duration.ofMinutes(enrollmentTtlMinutes));
    mfaSecretRepository.deleteAll(mfaSecretRepository.findByConfirmedFalseAndCreatedAtBefore(cutoff));
  }
}

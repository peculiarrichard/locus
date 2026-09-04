package com.locus.distraction.service;

import com.locus.distraction.repository.DistractionEventRepository;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// 18-month rolling retention purge for raw distraction rows, per technical-spec.md §9's T2
// window — runs the same locally as it would in production, no environment-specific branching.
@Component
public class RetentionPurgeJob {

  private static final Logger LOG = LoggerFactory.getLogger(RetentionPurgeJob.class);
  private static final int RETENTION_MONTHS = 18;

  private final DistractionEventRepository distractionEventRepository;

  public RetentionPurgeJob(DistractionEventRepository distractionEventRepository) {
    this.distractionEventRepository = distractionEventRepository;
  }

  @Scheduled(cron = "0 30 6 * * *")
  @Transactional
  public void purge() {
    Instant cutoff = ZonedDateTime.now(ZoneOffset.UTC).minusMonths(RETENTION_MONTHS).toInstant();
    long deleted = distractionEventRepository.deleteByOccurredAtBefore(cutoff);
    if (deleted > 0) {
      LOG.info("Retention purge removed {} distraction row(s) older than {} months", deleted, RETENTION_MONTHS);
    }
  }
}

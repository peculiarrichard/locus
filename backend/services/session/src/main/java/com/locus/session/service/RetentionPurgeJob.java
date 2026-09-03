package com.locus.session.service;

import com.locus.session.domain.SessionStatus;
import com.locus.session.repository.SessionRepository;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.EnumSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// 18-month rolling retention purge for raw session rows, per technical-spec.md §9's T2 window —
// runs the same locally as it would in production, no environment-specific branching. Only
// terminal (completed/abandoned) sessions are eligible, so a session somehow still active or
// paused after 18 months is never deleted purely on age.
@Component
public class RetentionPurgeJob {

  private static final Logger LOG = LoggerFactory.getLogger(RetentionPurgeJob.class);
  private static final int RETENTION_MONTHS = 18;
  private static final EnumSet<SessionStatus> TERMINAL_STATUSES = EnumSet.of(SessionStatus.COMPLETED,
      SessionStatus.ABANDONED);

  private final SessionRepository sessionRepository;

  public RetentionPurgeJob(SessionRepository sessionRepository) {
    this.sessionRepository = sessionRepository;
  }

  @Scheduled(cron = "0 30 6 * * *")
  @Transactional
  public void purge() {
    Instant cutoff = ZonedDateTime.now(ZoneOffset.UTC).minusMonths(RETENTION_MONTHS).toInstant();
    long deleted = sessionRepository.deleteByStatusInAndStartedAtBefore(TERMINAL_STATUSES, cutoff);
    if (deleted > 0) {
      LOG.info("Retention purge removed {} session row(s) older than {} months", deleted, RETENTION_MONTHS);
    }
  }
}

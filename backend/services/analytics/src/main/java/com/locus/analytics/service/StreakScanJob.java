package com.locus.analytics.service;

import com.locus.analytics.domain.SessionStatsDaily;
import com.locus.analytics.domain.Streak;
import com.locus.analytics.domain.UserLocale;
import com.locus.analytics.event.EventPublisher;
import com.locus.analytics.event.StreakBrokenPayload;
import com.locus.analytics.repository.SessionStatsDailyRepository;
import com.locus.analytics.repository.StreakRepository;
import com.locus.analytics.repository.UserLocaleRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// Daily scan detecting streak breaks, per frd.md: "current_streak_days increments on a qualifying
// day and resets on the first non-qualifying day, detected by Analytics' own daily scan." A
// single global cron fire evaluates each user's own "yesterday" in their own timezone rather than
// a blanket UTC day — a user very close to the international date line may see their day
// evaluated a little early or late relative to their own midnight, a reasonable v1 simplification
// given frd.md doesn't ask for per-user-timezone job staggering the way Notification Service's
// reminder job does.
@Component
public class StreakScanJob {

  private static final String DEFAULT_TIMEZONE = "Africa/Lagos";

  private final StreakRepository streakRepository;
  private final SessionStatsDailyRepository sessionStatsDailyRepository;
  private final UserLocaleRepository userLocaleRepository;
  private final EventPublisher eventPublisher;

  public StreakScanJob(
      StreakRepository streakRepository,
      SessionStatsDailyRepository sessionStatsDailyRepository,
      UserLocaleRepository userLocaleRepository,
      EventPublisher eventPublisher) {
    this.streakRepository = streakRepository;
    this.sessionStatsDailyRepository = sessionStatsDailyRepository;
    this.userLocaleRepository = userLocaleRepository;
    this.eventPublisher = eventPublisher;
  }

  @Scheduled(cron = "0 5 6 * * *")
  @Transactional
  public void scan() {
    for (Streak streak : streakRepository.findAll()) {
      LocalDate yesterday = yesterdayFor(streak.getUserId());
      if (streak.getLastQualifyingDay() != null && !streak.getLastQualifyingDay().isBefore(yesterday)) {
        continue;
      }
      boolean qualifies = sessionStatsDailyRepository
          .findById(new SessionStatsDaily.Key(streak.getUserId(), yesterday))
          .map(SessionStatsDaily::isHasQualifyingSession)
          .orElse(false);
      if (qualifies) {
        streak.extend(yesterday);
        streakRepository.save(streak);
      } else if (streak.getCurrentStreakDays() > 0) {
        int streakLengthBeforeBreak = streak.reset();
        streakRepository.save(streak);
        eventPublisher.publishStreakBroken(
            new StreakBrokenPayload(streak.getUserId(), streakLengthBeforeBreak, Instant.now()));
      }
    }
  }

  private LocalDate yesterdayFor(UUID userId) {
    String timezone = userLocaleRepository.findById(userId).map(UserLocale::getTimezone).orElse(DEFAULT_TIMEZONE);
    return LocalDate.now(ZoneId.of(timezone)).minusDays(1);
  }
}

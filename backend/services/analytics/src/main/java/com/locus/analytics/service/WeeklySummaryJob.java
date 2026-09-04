package com.locus.analytics.service;

import com.locus.analytics.domain.SessionStatsDaily;
import com.locus.analytics.domain.SessionStatsHourly;
import com.locus.analytics.domain.Streak;
import com.locus.analytics.event.EventPublisher;
import com.locus.analytics.event.WeeklySummaryDuePayload;
import com.locus.analytics.repository.SessionStatsDailyRepository;
import com.locus.analytics.repository.SessionStatsHourlyRepository;
import com.locus.analytics.repository.StreakRepository;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// Weekly scheduled job publishing WeeklySummaryDue with full summary content, per frd.md/technical-spec.md §7.
@Component
public class WeeklySummaryJob {

  private static final int TOP_HOURS_COUNT = 3;

  private final StreakRepository streakRepository;
  private final SessionStatsDailyRepository sessionStatsDailyRepository;
  private final SessionStatsHourlyRepository sessionStatsHourlyRepository;
  private final EventPublisher eventPublisher;

  public WeeklySummaryJob(
      StreakRepository streakRepository,
      SessionStatsDailyRepository sessionStatsDailyRepository,
      SessionStatsHourlyRepository sessionStatsHourlyRepository,
      EventPublisher eventPublisher) {
    this.streakRepository = streakRepository;
    this.sessionStatsDailyRepository = sessionStatsDailyRepository;
    this.sessionStatsHourlyRepository = sessionStatsHourlyRepository;
    this.eventPublisher = eventPublisher;
  }

  @Scheduled(cron = "0 0 7 * * MON")
  @Transactional(readOnly = true)
  public void publishWeeklySummaries() {
    LocalDate weekEnd = LocalDate.now(ZoneOffset.UTC).minusDays(1);
    LocalDate weekStart = weekEnd.minusDays(6);
    for (Streak streak : streakRepository.findAll()) {
      List<SessionStatsDaily> days = sessionStatsDailyRepository.findByUserIdAndStatDateBetween(streak.getUserId(),
          weekStart, weekEnd);
      if (days.isEmpty()) {
        continue;
      }
      int completed = days.stream().mapToInt(SessionStatsDaily::getSessionsCompleted).sum();
      int abandoned = days.stream().mapToInt(SessionStatsDaily::getSessionsAbandoned).sum();
      int distractionCount = days.stream().mapToInt(SessionStatsDaily::getDistractionCount).sum();
      double completionRate = (completed + abandoned) == 0 ? 0.0 : (double) completed / (completed + abandoned);

      List<Integer> bestHours = sessionStatsHourlyRepository
          .findByUserIdAndStatDateAfter(streak.getUserId(), weekStart.minusDays(1)).stream()
          .collect(Collectors.groupingBy(SessionStatsHourly::getHourOfDay,
              Collectors.summingLong(SessionStatsHourly::getFocusSeconds)))
          .entrySet()
          .stream()
          .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
          .limit(TOP_HOURS_COUNT)
          .map(Map.Entry::getKey)
          .toList();

      eventPublisher.publishWeeklySummaryDue(
          new WeeklySummaryDuePayload(streak.getUserId(), weekStart, weekEnd, bestHours, completionRate,
              distractionCount));
    }
  }
}

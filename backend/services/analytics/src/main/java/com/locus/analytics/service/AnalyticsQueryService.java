package com.locus.analytics.service;

import com.locus.analytics.domain.DistractionStatsHourly;
import com.locus.analytics.domain.SessionStatsDaily;
import com.locus.analytics.domain.SessionStatsHourly;
import com.locus.analytics.domain.Streak;
import com.locus.analytics.repository.DistractionStatsHourlyRepository;
import com.locus.analytics.repository.SessionStatsDailyRepository;
import com.locus.analytics.repository.SessionStatsHourlyRepository;
import com.locus.analytics.repository.StreakRepository;
import com.locus.analytics.web.dto.BestHourResponse;
import com.locus.analytics.web.dto.DistractionFrequencyResponse;
import com.locus.analytics.web.dto.HistoryDayResponse;
import com.locus.analytics.web.dto.SummaryResponse;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Read-only client-facing queries, per frd.md's Analytics Service API surface. Every endpoint
// returns a well-defined zero state for a new account rather than an error, per frd.md's
// explicit edge case.
@Service
public class AnalyticsQueryService {

  private static final int BEST_HOURS_WINDOW_DAYS = 90;

  private final StreakRepository streakRepository;
  private final SessionStatsDailyRepository sessionStatsDailyRepository;
  private final SessionStatsHourlyRepository sessionStatsHourlyRepository;
  private final DistractionStatsHourlyRepository distractionStatsHourlyRepository;

  public AnalyticsQueryService(
      StreakRepository streakRepository,
      SessionStatsDailyRepository sessionStatsDailyRepository,
      SessionStatsHourlyRepository sessionStatsHourlyRepository,
      DistractionStatsHourlyRepository distractionStatsHourlyRepository) {
    this.streakRepository = streakRepository;
    this.sessionStatsDailyRepository = sessionStatsDailyRepository;
    this.sessionStatsHourlyRepository = sessionStatsHourlyRepository;
    this.distractionStatsHourlyRepository = distractionStatsHourlyRepository;
  }

  @Transactional(readOnly = true)
  public SummaryResponse summary(UUID userId) {
    Streak streak = streakRepository.findById(userId).orElse(new Streak(userId));
    LocalDate today = LocalDate.now(ZoneOffset.UTC);
    LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1L);
    List<SessionStatsDaily> week = sessionStatsDailyRepository.findByUserIdAndStatDateBetween(userId, weekStart, today);
    int completed = week.stream().mapToInt(SessionStatsDaily::getSessionsCompleted).sum();
    int abandoned = week.stream().mapToInt(SessionStatsDaily::getSessionsAbandoned).sum();
    long focusSeconds = week.stream().mapToLong(SessionStatsDaily::getTotalFocusSeconds).sum();
    double completionRate = (completed + abandoned) == 0 ? 0.0 : (double) completed / (completed + abandoned);
    return new SummaryResponse(
        streak.getCurrentStreakDays(), streak.getLongestStreakDays(), completed, abandoned, focusSeconds,
        completionRate);
  }

  @Transactional(readOnly = true)
  public List<BestHourResponse> bestHours(UUID userId) {
    LocalDate since = LocalDate.now(ZoneOffset.UTC).minusDays(BEST_HOURS_WINDOW_DAYS);
    return sessionStatsHourlyRepository.findByUserIdAndStatDateAfter(userId, since).stream()
        .collect(Collectors.groupingBy(SessionStatsHourly::getHourOfDay,
            Collectors.summingLong(SessionStatsHourly::getFocusSeconds)))
        .entrySet()
        .stream()
        .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
        .map(e -> new BestHourResponse(e.getKey(), e.getValue()))
        .toList();
  }

  @Transactional(readOnly = true)
  public List<DistractionFrequencyResponse> distractionFrequency(UUID userId) {
    return distractionStatsHourlyRepository.findByUserId(userId).stream()
        .sorted(Comparator.comparingInt(DistractionStatsHourly::getHourOfDay))
        .map(
            bucket -> {
              double focusHours = bucket.getTotalFocusSecondsInBucket() / 3600.0;
              double rate = focusHours == 0 ? 0.0 : bucket.getDistractionCount() / focusHours;
              return new DistractionFrequencyResponse(
                  bucket.getHourOfDay(), bucket.getDistractionCount(), bucket.getTotalFocusSecondsInBucket(), rate);
            })
        .toList();
  }

  @Transactional(readOnly = true)
  public List<HistoryDayResponse> history(UUID userId, int days) {
    LocalDate to = LocalDate.now(ZoneOffset.UTC);
    LocalDate from = to.minusDays(days);
    return sessionStatsDailyRepository.findByUserIdAndStatDateBetween(userId, from, to).stream()
        .sorted(Comparator.comparing(SessionStatsDaily::getStatDate))
        .map(HistoryDayResponse::from)
        .toList();
  }
}

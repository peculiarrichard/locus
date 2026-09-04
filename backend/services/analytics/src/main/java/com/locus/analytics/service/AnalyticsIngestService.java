package com.locus.analytics.service;

import com.locus.analytics.domain.DistractionStatsHourly;
import com.locus.analytics.domain.SessionStatsDaily;
import com.locus.analytics.domain.SessionStatsHourly;
import com.locus.analytics.domain.Streak;
import com.locus.analytics.domain.UserLocale;
import com.locus.analytics.event.DistractionLoggedPayload;
import com.locus.analytics.event.SessionAbandonedPayload;
import com.locus.analytics.event.SessionCompletedPayload;
import com.locus.analytics.repository.DistractionStatsHourlyRepository;
import com.locus.analytics.repository.SessionStatsDailyRepository;
import com.locus.analytics.repository.SessionStatsHourlyRepository;
import com.locus.analytics.repository.StreakRepository;
import com.locus.analytics.repository.UserLocaleRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Ingests SessionCompleted/SessionAbandoned/DistractionLogged/UserProfileUpdated into Analytics'
// own denormalized tables, per frd.md's Analytics Service section. Streak evaluation itself is
// deliberately NOT done here — frd.md specifies it happens via Analytics' own daily scan, not
// inline on event receipt (see StreakScanJob) — this method only ensures a Streak row exists so
// the scan job has something to evaluate for a user's very first qualifying day.
@Service
public class AnalyticsIngestService {

  // The minimum single-session duration for a calendar day to "count" toward a streak, per
  // frd.md's qualifying-day rule. Not sized anywhere in frd.md — 20 minutes chosen as a floor
  // that filters out trivially short sessions without being an unreasonably high bar.
  static final long QUALIFYING_MIN_DURATION_SECONDS = 1200;

  private static final String DEFAULT_TIMEZONE = "Africa/Lagos";

  private final SessionStatsDailyRepository sessionStatsDailyRepository;
  private final SessionStatsHourlyRepository sessionStatsHourlyRepository;
  private final DistractionStatsHourlyRepository distractionStatsHourlyRepository;
  private final UserLocaleRepository userLocaleRepository;
  private final StreakRepository streakRepository;

  public AnalyticsIngestService(
      SessionStatsDailyRepository sessionStatsDailyRepository,
      SessionStatsHourlyRepository sessionStatsHourlyRepository,
      DistractionStatsHourlyRepository distractionStatsHourlyRepository,
      UserLocaleRepository userLocaleRepository,
      StreakRepository streakRepository) {
    this.sessionStatsDailyRepository = sessionStatsDailyRepository;
    this.sessionStatsHourlyRepository = sessionStatsHourlyRepository;
    this.distractionStatsHourlyRepository = distractionStatsHourlyRepository;
    this.userLocaleRepository = userLocaleRepository;
    this.streakRepository = streakRepository;
  }

  @Transactional
  public void recordSessionCompleted(SessionCompletedPayload payload) {
    ZoneId zone = zoneFor(payload.userId());
    ZonedDateTime startedAtLocal = payload.startedAt().atZone(zone);
    LocalDate day = startedAtLocal.toLocalDate();
    int hourOfDay = startedAtLocal.getHour();
    boolean qualifies = payload.durationSeconds() >= QUALIFYING_MIN_DURATION_SECONDS;

    SessionStatsDaily daily = loadOrCreateDaily(payload.userId(), day);
    daily.recordCompletedSession(payload.durationSeconds(), qualifies);
    sessionStatsDailyRepository.save(daily);

    SessionStatsHourly hourly = loadOrCreateHourly(payload.userId(), day, hourOfDay);
    hourly.addFocusSeconds(payload.durationSeconds());
    sessionStatsHourlyRepository.save(hourly);

    DistractionStatsHourly distractionBucket = loadOrCreateDistractionBucket(payload.userId(), hourOfDay);
    distractionBucket.addFocusSeconds(payload.durationSeconds());
    distractionStatsHourlyRepository.save(distractionBucket);

    if (streakRepository.findById(payload.userId()).isEmpty()) {
      streakRepository.save(new Streak(payload.userId()));
    }
  }

  @Transactional
  public void recordSessionAbandoned(SessionAbandonedPayload payload) {
    ZoneId zone = zoneFor(payload.userId());
    LocalDate day = payload.startedAt().atZone(zone).toLocalDate();
    SessionStatsDaily daily = loadOrCreateDaily(payload.userId(), day);
    daily.recordAbandonedSession();
    sessionStatsDailyRepository.save(daily);
  }

  @Transactional
  public void recordDistraction(DistractionLoggedPayload payload) {
    ZoneId zone = zoneFor(payload.userId());
    ZonedDateTime occurredAtLocal = payload.occurredAt().atZone(zone);
    DistractionStatsHourly bucket = loadOrCreateDistractionBucket(payload.userId(), occurredAtLocal.getHour());
    bucket.addDistraction();
    distractionStatsHourlyRepository.save(bucket);

    SessionStatsDaily daily = loadOrCreateDaily(payload.userId(), occurredAtLocal.toLocalDate());
    daily.recordDistraction();
    sessionStatsDailyRepository.save(daily);
  }

  @Transactional
  public void updateUserLocale(UUID userId, String timezone) {
    UserLocale locale = userLocaleRepository.findById(userId).orElseGet(() -> new UserLocale(userId, timezone));
    locale.setTimezone(timezone);
    userLocaleRepository.save(locale);
  }

  @Transactional
  public void purgeUser(UUID userId) {
    sessionStatsDailyRepository.deleteByUserId(userId);
    sessionStatsHourlyRepository.deleteByUserId(userId);
    distractionStatsHourlyRepository.deleteByUserId(userId);
    userLocaleRepository.deleteByUserId(userId);
    streakRepository.deleteById(userId);
  }

  private ZoneId zoneFor(UUID userId) {
    String timezone = userLocaleRepository.findById(userId).map(UserLocale::getTimezone).orElse(DEFAULT_TIMEZONE);
    return ZoneId.of(timezone);
  }

  private SessionStatsDaily loadOrCreateDaily(UUID userId, LocalDate day) {
    return sessionStatsDailyRepository
        .findById(new SessionStatsDaily.Key(userId, day))
        .orElseGet(() -> new SessionStatsDaily(userId, day));
  }

  private SessionStatsHourly loadOrCreateHourly(UUID userId, LocalDate day, int hourOfDay) {
    return sessionStatsHourlyRepository
        .findById(new SessionStatsHourly.Key(userId, day, hourOfDay))
        .orElseGet(() -> new SessionStatsHourly(userId, day, hourOfDay));
  }

  private DistractionStatsHourly loadOrCreateDistractionBucket(UUID userId, int hourOfDay) {
    return distractionStatsHourlyRepository
        .findById(new DistractionStatsHourly.Key(userId, hourOfDay))
        .orElseGet(() -> new DistractionStatsHourly(userId, hourOfDay));
  }
}

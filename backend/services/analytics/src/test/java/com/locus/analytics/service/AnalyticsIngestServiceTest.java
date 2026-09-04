package com.locus.analytics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.locus.analytics.domain.DistractionStatsHourly;
import com.locus.analytics.domain.SessionStatsDaily;
import com.locus.analytics.domain.SessionStatsHourly;
import com.locus.analytics.event.SessionAbandonedPayload;
import com.locus.analytics.event.SessionCompletedPayload;
import com.locus.analytics.repository.DistractionStatsHourlyRepository;
import com.locus.analytics.repository.SessionStatsDailyRepository;
import com.locus.analytics.repository.SessionStatsHourlyRepository;
import com.locus.analytics.repository.StreakRepository;
import com.locus.analytics.repository.UserLocaleRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class AnalyticsIngestServiceTest {

  private SessionStatsDailyRepository sessionStatsDailyRepository;
  private SessionStatsHourlyRepository sessionStatsHourlyRepository;
  private DistractionStatsHourlyRepository distractionStatsHourlyRepository;
  private UserLocaleRepository userLocaleRepository;
  private StreakRepository streakRepository;
  private AnalyticsIngestService service;
  private final UUID userId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    sessionStatsDailyRepository = mock(SessionStatsDailyRepository.class);
    sessionStatsHourlyRepository = mock(SessionStatsHourlyRepository.class);
    distractionStatsHourlyRepository = mock(DistractionStatsHourlyRepository.class);
    userLocaleRepository = mock(UserLocaleRepository.class);
    streakRepository = mock(StreakRepository.class);
    service = new AnalyticsIngestService(
        sessionStatsDailyRepository,
        sessionStatsHourlyRepository,
        distractionStatsHourlyRepository,
        userLocaleRepository,
        streakRepository);
    when(userLocaleRepository.findById(userId)).thenReturn(Optional.empty());
    when(sessionStatsDailyRepository.findById(any())).thenReturn(Optional.empty());
    when(sessionStatsHourlyRepository.findById(any())).thenReturn(Optional.empty());
    when(distractionStatsHourlyRepository.findById(any())).thenReturn(Optional.empty());
    when(streakRepository.findById(userId)).thenReturn(Optional.empty());
  }

  @Test
  void aQualifyingSessionMarksHasQualifyingSessionTrue() {
    Instant startedAt = Instant.parse("2026-01-01T09:00:00Z");
    SessionCompletedPayload payload = new SessionCompletedPayload(userId, UUID.randomUUID(), "DEEP_WORK", startedAt,
        startedAt.plusSeconds(1800), 1800, null);

    service.recordSessionCompleted(payload);

    ArgumentCaptor<SessionStatsDaily> captor = ArgumentCaptor.forClass(SessionStatsDaily.class);
    Mockito.verify(sessionStatsDailyRepository).save(captor.capture());
    assertThat(captor.getValue().isHasQualifyingSession()).isTrue();
    assertThat(captor.getValue().getStatDate()).isEqualTo(LocalDate.of(2026, 1, 1));
  }

  @Test
  void aBelowFloorSessionDoesNotMarkQualifying() {
    Instant startedAt = Instant.parse("2026-01-01T09:00:00Z");
    SessionCompletedPayload payload = new SessionCompletedPayload(userId, UUID.randomUUID(), "POMODORO", startedAt,
        startedAt.plusSeconds(300), 300, null);

    service.recordSessionCompleted(payload);

    ArgumentCaptor<SessionStatsDaily> captor = ArgumentCaptor.forClass(SessionStatsDaily.class);
    Mockito.verify(sessionStatsDailyRepository).save(captor.capture());
    assertThat(captor.getValue().isHasQualifyingSession()).isFalse();
  }

  @Test
  void sessionCompletedAlsoUpdatesHourlyFocusSeconds() {
    // 14:00 UTC is 15:00 in the default WAT (UTC+1) zone applied when no UserLocale is known.
    Instant startedAt = Instant.parse("2026-01-01T14:00:00Z");
    SessionCompletedPayload payload = new SessionCompletedPayload(userId, UUID.randomUUID(), "DEEP_WORK", startedAt,
        startedAt.plusSeconds(1800), 1800, null);

    service.recordSessionCompleted(payload);

    ArgumentCaptor<SessionStatsHourly> captor = ArgumentCaptor.forClass(SessionStatsHourly.class);
    Mockito.verify(sessionStatsHourlyRepository).save(captor.capture());
    assertThat(captor.getValue().getHourOfDay()).isEqualTo(15);
    assertThat(captor.getValue().getFocusSeconds()).isEqualTo(1800);
  }

  @Test
  void abandonedSessionIncrementsAbandonedCountForItsStartDay() {
    Instant startedAt = Instant.parse("2026-01-01T09:00:00Z");
    service.recordSessionAbandoned(
        new SessionAbandonedPayload(userId, UUID.randomUUID(), startedAt, startedAt.plusSeconds(60), 60));

    ArgumentCaptor<SessionStatsDaily> captor = ArgumentCaptor.forClass(SessionStatsDaily.class);
    Mockito.verify(sessionStatsDailyRepository).save(captor.capture());
    assertThat(captor.getValue().getSessionsAbandoned()).isEqualTo(1);
  }

  @Test
  void distractionUpdatesBothHourlyBucketAndDailyCount() {
    // 10:30 UTC is 11:30 in the default WAT (UTC+1) zone applied when no UserLocale is known.
    Instant occurredAt = Instant.parse("2026-01-01T10:30:00Z");
    service.recordDistraction(new com.locus.analytics.event.DistractionLoggedPayload(userId, UUID.randomUUID(),
        UUID.randomUUID(), occurredAt));

    ArgumentCaptor<DistractionStatsHourly> bucketCaptor = ArgumentCaptor.forClass(DistractionStatsHourly.class);
    Mockito.verify(distractionStatsHourlyRepository).save(bucketCaptor.capture());
    assertThat(bucketCaptor.getValue().getHourOfDay()).isEqualTo(11);
    assertThat(bucketCaptor.getValue().getDistractionCount()).isEqualTo(1);

    ArgumentCaptor<SessionStatsDaily> dailyCaptor = ArgumentCaptor.forClass(SessionStatsDaily.class);
    Mockito.verify(sessionStatsDailyRepository).save(dailyCaptor.capture());
    assertThat(dailyCaptor.getValue().getDistractionCount()).isEqualTo(1);
  }

  @Test
  void zoneOffsetDefaultsToWestAfricaTimeWhenNoLocaleKnown() {
    // WAT is UTC+1 — 23:30 UTC on Jan 1 is 00:30 WAT on Jan 2, so the day/hour attribution
    // should roll over, proving the default timezone (not UTC) is actually being applied.
    Instant startedAt = Instant.parse("2026-01-01T23:30:00Z");
    SessionCompletedPayload payload = new SessionCompletedPayload(userId, UUID.randomUUID(), "DEEP_WORK", startedAt,
        startedAt.plusSeconds(1800), 1800, null);

    service.recordSessionCompleted(payload);

    ArgumentCaptor<SessionStatsDaily> captor = ArgumentCaptor.forClass(SessionStatsDaily.class);
    Mockito.verify(sessionStatsDailyRepository).save(captor.capture());
    assertThat(captor.getValue().getStatDate()).isEqualTo(LocalDate.of(2026, 1, 2));
  }
}

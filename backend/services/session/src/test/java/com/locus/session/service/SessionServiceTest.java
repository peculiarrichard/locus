package com.locus.session.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.locus.session.domain.Session;
import com.locus.session.domain.SessionStatus;
import com.locus.session.domain.SessionType;
import com.locus.session.event.EventPublisher;
import com.locus.session.exception.ApiException;
import com.locus.session.repository.SessionRepository;
import com.locus.session.web.dto.StartSessionRequest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class SessionServiceTest {

  private SessionRepository sessionRepository;
  private EventPublisher eventPublisher;
  private SessionService sessionService;
  private final UUID userId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    sessionRepository = mock(SessionRepository.class);
    eventPublisher = mock(EventPublisher.class);
    sessionService = new SessionService(sessionRepository, eventPublisher);
    when(sessionRepository.save(any(Session.class))).thenAnswer(invocation -> invocation.getArgument(0));
  }

  @Test
  void startCreatesAnActiveSession() {
    StartSessionRequest request = new StartSessionRequest(SessionType.DEEP_WORK, 3600, null, null, null, null);
    Session session = sessionService.start(userId, request);
    assertThat(session.getStatus()).isEqualTo(SessionStatus.ACTIVE);
    assertThat(session.getUserId()).isEqualTo(userId);
  }

  @Test
  void pausingAnAlreadyPausedSessionIs409() {
    Session session = activeSession();
    session.setStatus(SessionStatus.PAUSED);
    when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));

    assertThatThrownBy(() -> sessionService.pause(userId, session.getId()))
        .isInstanceOf(ApiException.class)
        .extracting("errorCode")
        .isEqualTo("INVALID_STATE");
  }

  @Test
  void resumeFoldsElapsedPauseTimeIntoAccumulatedPauseSeconds() {
    Session session = activeSession();
    session.setStatus(SessionStatus.PAUSED);
    session.setPausedAt(Instant.now().minus(10, ChronoUnit.SECONDS));
    when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));

    Session resumed = sessionService.resume(userId, session.getId());

    assertThat(resumed.getStatus()).isEqualTo(SessionStatus.ACTIVE);
    assertThat(resumed.getAccumulatedPauseSeconds()).isGreaterThanOrEqualTo(10);
    assertThat(resumed.getPausedAt()).isNull();
  }

  @Test
  void endingAnAlreadyTerminalSessionIs409() {
    Session session = activeSession();
    session.setStatus(SessionStatus.COMPLETED);
    when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));

    assertThatThrownBy(() -> sessionService.end(userId, session.getId()))
        .isInstanceOf(ApiException.class)
        .extracting("errorCode")
        .isEqualTo("INVALID_STATE");
    verifyNoInteractions(eventPublisher);
  }

  @Test
  void endExcludesAccumulatedPauseTimeFromDuration() {
    Session session = activeSession();
    ReflectionTestUtils.setField(session, "startedAt", Instant.now().minus(100, ChronoUnit.SECONDS));
    session.setAccumulatedPauseSeconds(30);
    when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));

    Session ended = sessionService.end(userId, session.getId());

    assertThat(ended.getStatus()).isEqualTo(SessionStatus.COMPLETED);
    assertThat(ended.getDurationSeconds()).isBetween(65, 75);
  }

  @Test
  void abandonUsesClientSuppliedTimestampWhenGiven() {
    Session session = activeSession();
    Instant startedAt = Instant.now().minus(200, ChronoUnit.SECONDS);
    ReflectionTestUtils.setField(session, "startedAt", startedAt);
    when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
    Instant reconciledAt = startedAt.plusSeconds(50);

    Session abandoned = sessionService.abandon(userId, session.getId(), reconciledAt);

    assertThat(abandoned.getStatus()).isEqualTo(SessionStatus.ABANDONED);
    assertThat(abandoned.getAbandonedAt()).isEqualTo(reconciledAt);
  }

  @Test
  void gettingAnotherUsersSessionIsForbidden() {
    Session session = activeSession();
    when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));

    assertThatThrownBy(() -> sessionService.get(UUID.randomUUID(), session.getId()))
        .isInstanceOf(ApiException.class)
        .extracting("errorCode")
        .isEqualTo("NOT_YOUR_SESSION");
  }

  private Session activeSession() {
    Session session = new Session(userId, SessionType.POMODORO, 1500, null, null, null, null);
    ReflectionTestUtils.setField(session, "id", UUID.randomUUID());
    return session;
  }
}

package com.locus.distraction.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.locus.distraction.client.SessionInfo;
import com.locus.distraction.client.SessionServiceClient;
import com.locus.distraction.domain.DistractionEvent;
import com.locus.distraction.event.EventPublisher;
import com.locus.distraction.exception.ApiException;
import com.locus.distraction.repository.DistractionEventRepository;
import com.locus.distraction.web.dto.LogDistractionRequest;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DistractionServiceTest {

  private DistractionEventRepository repository;
  private SessionServiceClient sessionServiceClient;
  private EventPublisher eventPublisher;
  private DistractionService distractionService;
  private final UUID userId = UUID.randomUUID();
  private final UUID sessionId = UUID.randomUUID();
  private final String bearerToken = "Bearer test-token";

  @BeforeEach
  void setUp() {
    repository = mock(DistractionEventRepository.class);
    sessionServiceClient = mock(SessionServiceClient.class);
    eventPublisher = mock(EventPublisher.class);
    distractionService = new DistractionService(repository, sessionServiceClient, eventPublisher);
  }

  @Test
  void duplicateSubmissionIsANoOpUpsert() {
    UUID distractionId = UUID.randomUUID();
    DistractionEvent existing = new DistractionEvent(distractionId, userId, sessionId, Instant.now(), 10);
    when(repository.findById(distractionId)).thenReturn(Optional.of(existing));

    LogDistractionRequest request = new LogDistractionRequest(distractionId, sessionId, Instant.now(), 10);
    DistractionEvent result = distractionService.record(userId, bearerToken, request);

    assertThat(result).isSameAs(existing);
    verify(sessionServiceClient, never()).fetchOwnedSession(any(), any());
    verify(eventPublisher, never()).publishDistractionLogged(any());
  }

  @Test
  void belowThresholdDurationIsRejected() {
    LogDistractionRequest request = new LogDistractionRequest(UUID.randomUUID(), sessionId, Instant.now(), 2);
    when(repository.findById(request.id())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> distractionService.record(userId, bearerToken, request))
        .isInstanceOf(ApiException.class)
        .extracting("errorCode")
        .isEqualTo("DURATION_TOO_SHORT");
  }

  @Test
  void occurredAtBeforeSessionStartIsRejected() {
    Instant sessionStart = Instant.now();
    when(sessionServiceClient.fetchOwnedSession(bearerToken, sessionId))
        .thenReturn(new SessionInfo(sessionStart, null, "ACTIVE"));
    LogDistractionRequest request = new LogDistractionRequest(UUID.randomUUID(), sessionId,
        sessionStart.minusSeconds(60), 10);
    when(repository.findById(request.id())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> distractionService.record(userId, bearerToken, request))
        .isInstanceOf(ApiException.class)
        .extracting("errorCode")
        .isEqualTo("OCCURRED_AT_OUT_OF_RANGE");
  }

  @Test
  void notOwnedSessionPropagatesForbidden() {
    when(sessionServiceClient.fetchOwnedSession(eq(bearerToken), eq(sessionId)))
        .thenThrow(ApiException.forbidden("SESSION_NOT_OWNED", "not yours"));
    LogDistractionRequest request = new LogDistractionRequest(UUID.randomUUID(), sessionId, Instant.now(), 10);
    when(repository.findById(request.id())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> distractionService.record(userId, bearerToken, request))
        .isInstanceOf(ApiException.class)
        .extracting("errorCode")
        .isEqualTo("SESSION_NOT_OWNED");
  }

  @Test
  void validSubmissionIsSavedAndPublished() {
    Instant sessionStart = Instant.now().minusSeconds(120);
    when(sessionServiceClient.fetchOwnedSession(bearerToken, sessionId))
        .thenReturn(new SessionInfo(sessionStart, null, "ACTIVE"));
    LogDistractionRequest request = new LogDistractionRequest(UUID.randomUUID(), sessionId,
        sessionStart.plusSeconds(30), 10);
    when(repository.findById(request.id())).thenReturn(Optional.empty());

    DistractionEvent result = distractionService.record(userId, bearerToken, request);

    assertThat(result.getUserId()).isEqualTo(userId);
    assertThat(result.getDurationSeconds()).isEqualTo(10);
    verify(eventPublisher).publishDistractionLogged(any());
  }
}

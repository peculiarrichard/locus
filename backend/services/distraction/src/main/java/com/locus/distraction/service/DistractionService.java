package com.locus.distraction.service;

import com.locus.distraction.client.SessionInfo;
import com.locus.distraction.client.SessionServiceClient;
import com.locus.distraction.domain.DistractionEvent;
import com.locus.distraction.event.DistractionLoggedPayload;
import com.locus.distraction.event.EventPublisher;
import com.locus.distraction.exception.ApiException;
import com.locus.distraction.repository.DistractionEventRepository;
import com.locus.distraction.web.dto.LogDistractionRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Distraction event ingestion, per frd.md's Distraction Logging Service section.
@Service
public class DistractionService {

  // "Small grace window" per frd.md, deliberately unspecified there — 5 minutes absorbs client
  // clock skew and in-flight request latency without meaningfully weakening the ownership check.
  private static final Duration GRACE_WINDOW = Duration.ofMinutes(5);
  private static final int MIN_DURATION_SECONDS = 3;

  private final DistractionEventRepository distractionEventRepository;
  private final SessionServiceClient sessionServiceClient;
  private final EventPublisher eventPublisher;

  public DistractionService(
      DistractionEventRepository distractionEventRepository,
      SessionServiceClient sessionServiceClient,
      EventPublisher eventPublisher) {
    this.distractionEventRepository = distractionEventRepository;
    this.sessionServiceClient = sessionServiceClient;
    this.eventPublisher = eventPublisher;
  }

  @Transactional
  public DistractionEvent record(UUID userId, String bearerToken, LogDistractionRequest request) {
    return distractionEventRepository
        .findById(request.id())
        // Duplicate submission from the client's retry queue: no-op upsert, per frd.md.
        .orElseGet(() -> insert(userId, bearerToken, request));
  }

  private DistractionEvent insert(UUID userId, String bearerToken, LogDistractionRequest request) {
    if (request.durationSeconds() < MIN_DURATION_SECONDS) {
      throw ApiException.badRequest(
          "DURATION_TOO_SHORT", "Distraction duration below the minimum submission threshold");
    }

    SessionInfo session = sessionServiceClient.fetchOwnedSession(bearerToken, request.sessionId());
    Instant windowEnd = (session.completedAt() != null ? session.completedAt() : Instant.now()).plus(GRACE_WINDOW);
    if (request.occurredAt().isBefore(session.startedAt()) || request.occurredAt().isAfter(windowEnd)) {
      throw ApiException.badRequest(
          "OCCURRED_AT_OUT_OF_RANGE", "occurredAt falls outside the session's active time window");
    }

    DistractionEvent event = new DistractionEvent(
        request.id(), userId, request.sessionId(), request.occurredAt(), request.durationSeconds());
    try {
      distractionEventRepository.save(event);
    } catch (DataIntegrityViolationException e) {
      // A concurrent retry of the same distraction_id won the race; treat as idempotent success.
      return distractionEventRepository.findById(request.id()).orElseThrow(() -> e);
    }
    eventPublisher.publishDistractionLogged(
        new DistractionLoggedPayload(userId, request.sessionId(), request.id(), request.occurredAt()));
    return event;
  }

  @Transactional(readOnly = true)
  public List<DistractionEvent> listForSession(UUID userId, UUID sessionId) {
    return distractionEventRepository.findByUserIdAndSessionIdOrderByOccurredAt(userId, sessionId);
  }
}

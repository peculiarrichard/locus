package com.locus.session.service;

import com.locus.session.domain.Session;
import com.locus.session.domain.SessionStatus;
import com.locus.session.event.EventPublisher;
import com.locus.session.event.SessionAbandonedPayload;
import com.locus.session.event.SessionCompletedPayload;
import com.locus.session.exception.ApiException;
import com.locus.session.repository.SessionRepository;
import com.locus.session.web.dto.StartSessionRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Session lifecycle: start/pause/resume/end/abandon, per frd.md's Session Service section.
@Service
public class SessionService {

  private final SessionRepository sessionRepository;
  private final EventPublisher eventPublisher;

  public SessionService(SessionRepository sessionRepository, EventPublisher eventPublisher) {
    this.sessionRepository = sessionRepository;
    this.eventPublisher = eventPublisher;
  }

  @Transactional
  public Session start(UUID userId, StartSessionRequest request) {
    Session session = new Session(
        userId,
        request.sessionType(),
        request.plannedDurationSeconds(),
        request.goalId(),
        request.workMinutes(),
        request.breakMinutes(),
        request.cycleCount());
    // The one-non-terminal-session-per-user rule is enforced by V1__init.sql's partial unique
    // index; a violation surfaces as DataIntegrityViolationException, mapped to 409 by
    // GlobalExceptionHandler.
    return sessionRepository.save(session);
  }

  @Transactional
  public Session pause(UUID userId, UUID sessionId) {
    Session session = getOwned(userId, sessionId);
    if (session.getStatus() != SessionStatus.ACTIVE) {
      throw ApiException.conflict("INVALID_STATE", "Session is not active");
    }
    session.setStatus(SessionStatus.PAUSED);
    session.setPausedAt(Instant.now());
    return sessionRepository.save(session);
  }

  @Transactional
  public Session resume(UUID userId, UUID sessionId) {
    Session session = getOwned(userId, sessionId);
    if (session.getStatus() != SessionStatus.PAUSED) {
      throw ApiException.conflict("INVALID_STATE", "Session is not paused");
    }
    session.setAccumulatedPauseSeconds(
        session.getAccumulatedPauseSeconds()
            + (int) Duration.between(session.getPausedAt(), Instant.now()).getSeconds());
    session.setPausedAt(null);
    session.setStatus(SessionStatus.ACTIVE);
    return sessionRepository.save(session);
  }

  @Transactional
  public Session end(UUID userId, UUID sessionId) {
    Session session = getOwned(userId, sessionId);
    if (session.isTerminal()) {
      throw ApiException.conflict("INVALID_STATE", "Session is already terminal");
    }
    Instant now = Instant.now();
    int pauseSeconds = foldOngoingPause(session, now);
    int durationSeconds = (int) Duration.between(session.getStartedAt(), now).getSeconds() - pauseSeconds;
    session.setAccumulatedPauseSeconds(pauseSeconds);
    session.setCompletedAt(now);
    session.setDurationSeconds(durationSeconds);
    session.setStatus(SessionStatus.COMPLETED);
    Session saved = sessionRepository.save(session);
    eventPublisher.publishSessionCompleted(
        new SessionCompletedPayload(
            userId,
            saved.getId(),
            saved.getSessionType().name(),
            saved.getStartedAt(),
            now,
            durationSeconds,
            saved.getGoalId()));
    return saved;
  }

  @Transactional
  public Session abandon(UUID userId, UUID sessionId, Instant clientAbandonedAt) {
    Session session = getOwned(userId, sessionId);
    if (session.isTerminal()) {
      throw ApiException.conflict("INVALID_STATE", "Session is already terminal");
    }
    // Reconciliation from a crashed/force-quit client supplies its own last-known timestamp
    // rather than a server-side guess, per frd.md's orphaned-session edge case.
    Instant abandonedAt = clientAbandonedAt != null ? clientAbandonedAt : Instant.now();
    int elapsedSeconds = (int) Duration.between(session.getStartedAt(), abandonedAt).getSeconds();
    session.setAbandonedAt(abandonedAt);
    session.setStatus(SessionStatus.ABANDONED);
    Session saved = sessionRepository.save(session);
    eventPublisher.publishSessionAbandoned(
        new SessionAbandonedPayload(
            userId, saved.getId(), saved.getStartedAt(), abandonedAt, elapsedSeconds));
    return saved;
  }

  @Transactional(readOnly = true)
  public Session get(UUID userId, UUID sessionId) {
    return getOwned(userId, sessionId);
  }

  @Transactional(readOnly = true)
  public List<Session> list(UUID userId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startedAt"));
    return sessionRepository.findByUserIdOrderByStartedAtDesc(userId, pageable);
  }

  private int foldOngoingPause(Session session, Instant now) {
    int pauseSeconds = session.getAccumulatedPauseSeconds();
    if (session.getStatus() == SessionStatus.PAUSED && session.getPausedAt() != null) {
      pauseSeconds += (int) Duration.between(session.getPausedAt(), now).getSeconds();
    }
    return pauseSeconds;
  }

  private Session getOwned(UUID userId, UUID sessionId) {
    Session session = sessionRepository
        .findById(sessionId)
        .orElseThrow(() -> ApiException.notFound("Session"));
    if (!session.getUserId().equals(userId)) {
      throw ApiException.forbidden("NOT_YOUR_SESSION", "Session does not belong to this user");
    }
    return session;
  }
}

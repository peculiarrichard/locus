package com.locus.session.web;

import com.locus.session.domain.Session;
import com.locus.session.service.SessionService;
import com.locus.session.web.dto.AbandonSessionRequest;
import com.locus.session.web.dto.SessionResponse;
import com.locus.session.web.dto.StartSessionRequest;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// Session lifecycle API surface, per frd.md's Session Service section.
@RestController
public class SessionController {

  private final SessionService sessionService;

  public SessionController(SessionService sessionService) {
    this.sessionService = sessionService;
  }

  @PostMapping("/sessions/start")
  @ResponseStatus(HttpStatus.CREATED)
  public SessionResponse start(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody StartSessionRequest request) {
    Session session = sessionService.start(UUID.fromString(jwt.getSubject()), request);
    return SessionResponse.from(session);
  }

  @PostMapping("/sessions/{id}/pause")
  public SessionResponse pause(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") UUID sessionId) {
    Session session = sessionService.pause(UUID.fromString(jwt.getSubject()), sessionId);
    return SessionResponse.from(session);
  }

  @PostMapping("/sessions/{id}/resume")
  public SessionResponse resume(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") UUID sessionId) {
    Session session = sessionService.resume(UUID.fromString(jwt.getSubject()), sessionId);
    return SessionResponse.from(session);
  }

  @PostMapping("/sessions/{id}/end")
  public SessionResponse end(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") UUID sessionId) {
    Session session = sessionService.end(UUID.fromString(jwt.getSubject()), sessionId);
    return SessionResponse.from(session);
  }

  @PostMapping("/sessions/{id}/abandon")
  public SessionResponse abandon(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable("id") UUID sessionId,
      @RequestBody(required = false) AbandonSessionRequest request) {
    Instant clientAbandonedAt = request != null ? request.abandonedAt() : null;
    Session session = sessionService.abandon(UUID.fromString(jwt.getSubject()), sessionId, clientAbandonedAt);
    return SessionResponse.from(session);
  }

  @GetMapping("/sessions/{id}")
  public SessionResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") UUID sessionId) {
    return SessionResponse.from(sessionService.get(UUID.fromString(jwt.getSubject()), sessionId));
  }

  @GetMapping("/sessions")
  public List<SessionResponse> list(
      @AuthenticationPrincipal Jwt jwt,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return sessionService.list(UUID.fromString(jwt.getSubject()), page, size).stream()
        .map(SessionResponse::from)
        .toList();
  }
}

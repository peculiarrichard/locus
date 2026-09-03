package com.locus.distraction.web;

import com.locus.distraction.service.DistractionService;
import com.locus.distraction.web.dto.DistractionResponse;
import com.locus.distraction.web.dto.LogDistractionRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// Distraction event ingestion API surface, per frd.md's Distraction Logging Service section.
@RestController
public class DistractionController {

  private final DistractionService distractionService;

  public DistractionController(DistractionService distractionService) {
    this.distractionService = distractionService;
  }

  @PostMapping("/distractions")
  @ResponseStatus(HttpStatus.CREATED)
  public DistractionResponse log(
      @AuthenticationPrincipal Jwt jwt,
      @RequestHeader("Authorization") String authorization,
      @Valid @RequestBody LogDistractionRequest request) {
    return DistractionResponse.from(
        distractionService.record(UUID.fromString(jwt.getSubject()), authorization, request));
  }

  @GetMapping("/distractions")
  public List<DistractionResponse> list(
      @AuthenticationPrincipal Jwt jwt, @RequestParam("session_id") UUID sessionId) {
    return distractionService.listForSession(UUID.fromString(jwt.getSubject()), sessionId).stream()
        .map(DistractionResponse::from)
        .toList();
  }
}

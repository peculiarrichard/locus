package com.locus.analytics.web;

import com.locus.analytics.service.AnalyticsQueryService;
import com.locus.analytics.web.dto.BestHourResponse;
import com.locus.analytics.web.dto.DistractionFrequencyResponse;
import com.locus.analytics.web.dto.HistoryDayResponse;
import com.locus.analytics.web.dto.SummaryResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Read-only client-facing API surface, per frd.md's Analytics Service section.
@RestController
public class AnalyticsController {

  private final AnalyticsQueryService analyticsQueryService;

  public AnalyticsController(AnalyticsQueryService analyticsQueryService) {
    this.analyticsQueryService = analyticsQueryService;
  }

  @GetMapping("/analytics/summary")
  public SummaryResponse summary(@AuthenticationPrincipal Jwt jwt) {
    return analyticsQueryService.summary(UUID.fromString(jwt.getSubject()));
  }

  @GetMapping("/analytics/best-hours")
  public List<BestHourResponse> bestHours(@AuthenticationPrincipal Jwt jwt) {
    return analyticsQueryService.bestHours(UUID.fromString(jwt.getSubject()));
  }

  @GetMapping("/analytics/distraction-frequency")
  public List<DistractionFrequencyResponse> distractionFrequency(@AuthenticationPrincipal Jwt jwt) {
    return analyticsQueryService.distractionFrequency(UUID.fromString(jwt.getSubject()));
  }

  @GetMapping("/analytics/history")
  public List<HistoryDayResponse> history(
      @AuthenticationPrincipal Jwt jwt, @RequestParam(defaultValue = "30") int range) {
    return analyticsQueryService.history(UUID.fromString(jwt.getSubject()), range);
  }
}

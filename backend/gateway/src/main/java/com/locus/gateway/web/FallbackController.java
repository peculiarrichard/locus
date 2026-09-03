package com.locus.gateway.web;

import com.locus.gateway.filter.CorrelationIdFilter;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

// Circuit-breaker fallback target — returns the standardized error envelope instead of a raw
// connection reset when a backend route's circuit is open, per frd.md's API Gateway edge cases.
@RestController
public class FallbackController {

  private static final String MESSAGE = "Backend service is currently unavailable";
  private static final HttpStatus STATUS = HttpStatus.SERVICE_UNAVAILABLE;

  @RequestMapping("/gateway/fallback")
  public ResponseEntity<ErrorEnvelope> fallback(ServerWebExchange exchange) {
    Object id = exchange.getAttribute(CorrelationIdFilter.ATTRIBUTE);
    String corrId = id != null ? id.toString() : UUID.randomUUID().toString();
    ErrorEnvelope envelope = new ErrorEnvelope("SERVICE_UNAVAILABLE", MESSAGE, corrId, Instant.now());
    return ResponseEntity.status(STATUS).body(envelope);
  }
}

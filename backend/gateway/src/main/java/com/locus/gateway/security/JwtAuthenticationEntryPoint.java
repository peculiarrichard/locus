package com.locus.gateway.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.locus.gateway.filter.CorrelationIdFilter;
import com.locus.gateway.web.ErrorEnvelope;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

// Distinguishes an expired access token from a missing/invalid one, per frd.md's API Gateway edge
// case — the client needs a different error code to know whether to attempt a silent refresh or
// force a full re-login.
@Component
public class JwtAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
    String errorCode = "MISSING_TOKEN";
    String message = "Authentication is required";
    if (ex instanceof OAuth2AuthenticationException oauthEx) {
      OAuth2Error error = oauthEx.getError();
      String description = error.getDescription() == null ? "" : error.getDescription().toLowerCase();
      if (description.contains("expired")) {
        errorCode = "TOKEN_EXPIRED";
        message = "Access token has expired";
      } else {
        errorCode = "INVALID_TOKEN";
        message = "Access token is invalid";
      }
    }
    ServerHttpResponse response = exchange.getResponse();
    response.setStatusCode(HttpStatus.UNAUTHORIZED);
    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
    Object id = exchange.getAttribute(CorrelationIdFilter.ATTRIBUTE);
    String correlationId = id != null ? id.toString() : UUID.randomUUID().toString();
    ErrorEnvelope envelope = new ErrorEnvelope(errorCode, message, correlationId, Instant.now());
    byte[] bytes;
    try {
      bytes = objectMapper.writeValueAsBytes(envelope);
    } catch (Exception e) {
      bytes = "{}".getBytes();
    }
    return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
  }
}

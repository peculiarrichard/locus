package com.locus.distraction.client;

import com.locus.distraction.exception.ApiException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

// Synchronously validates session ownership/time-window with Session Service, per frd.md's
// validation rule — this is the one place this service needs live authoritative state rather
// than an eventually-consistent local copy, so it stays a direct call instead of event-carried
// state transfer.
@Component
public class SessionServiceClient {

  private static final Logger LOG = LoggerFactory.getLogger(SessionServiceClient.class);

  private final RestClient restClient;

  public SessionServiceClient(
      RestClient.Builder builder, @Value("${locus.session-service.base-url}") String baseUrl) {
    this.restClient = builder.baseUrl(baseUrl).build();
  }

  public SessionInfo fetchOwnedSession(String bearerToken, UUID sessionId) {
    try {
      return restClient
          .get()
          .uri("/sessions/{id}", sessionId)
          .header(HttpHeaders.AUTHORIZATION, bearerToken)
          .retrieve()
          .body(SessionInfo.class);
    } catch (HttpClientErrorException.NotFound | HttpClientErrorException.Forbidden e) {
      LOG.warn("Rejected distraction submission for session {} not owned by caller", sessionId);
      throw ApiException.forbidden("SESSION_NOT_OWNED", "Session does not belong to this user");
    }
  }
}

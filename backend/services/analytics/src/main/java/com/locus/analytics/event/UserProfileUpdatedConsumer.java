package com.locus.analytics.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.locus.analytics.service.AnalyticsIngestService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.stereotype.Component;

@Component
public class UserProfileUpdatedConsumer {

  private static final TypeReference<EventEnvelope<UserProfileUpdatedPayload>> EVENT_TYPE = new TypeReference<>() {
  };

  private final AnalyticsIngestService analyticsIngestService;
  private final ObjectMapper objectMapper;

  public UserProfileUpdatedConsumer(AnalyticsIngestService analyticsIngestService, ObjectMapper objectMapper) {
    this.analyticsIngestService = analyticsIngestService;
    this.objectMapper = objectMapper;
  }

  @SqsListener("analytics-user-profile-updated-queue")
  public void onUserProfileUpdated(String rawMessage) throws JsonProcessingException {
    UserProfileUpdatedPayload payload = objectMapper.readValue(rawMessage, EVENT_TYPE).payload();
    ConflictRetry.run(() -> analyticsIngestService.updateUserLocale(payload.userId(), payload.timezone()));
  }
}

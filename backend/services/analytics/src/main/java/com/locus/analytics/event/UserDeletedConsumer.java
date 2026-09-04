package com.locus.analytics.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.locus.analytics.service.AnalyticsIngestService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.stereotype.Component;

// Consumes UserDeleted to purge this user's analytics data, per technical-spec.md §9's erasure cascade.
@Component
public class UserDeletedConsumer {

  private static final TypeReference<EventEnvelope<UserDeletedPayload>> EVENT_TYPE = new TypeReference<>() {
  };

  private final AnalyticsIngestService analyticsIngestService;
  private final ObjectMapper objectMapper;

  public UserDeletedConsumer(AnalyticsIngestService analyticsIngestService, ObjectMapper objectMapper) {
    this.analyticsIngestService = analyticsIngestService;
    this.objectMapper = objectMapper;
  }

  @SqsListener("analytics-user-deleted-queue")
  public void onUserDeleted(String rawMessage) throws JsonProcessingException {
    analyticsIngestService.purgeUser(objectMapper.readValue(rawMessage, EVENT_TYPE).payload().userId());
  }
}

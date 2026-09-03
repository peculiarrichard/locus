package com.locus.accountability.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.locus.accountability.service.AccountabilityService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.stereotype.Component;

// Consumes UserDeleted to dissolve pairings/groups and purge this user's data, per frd.md's
// pairing-dissolution edge case and technical-spec.md §9's erasure cascade.
@Component
public class UserDeletedConsumer {

  private static final TypeReference<EventEnvelope<UserDeletedPayload>> EVENT_TYPE = new TypeReference<>() {
  };

  private final AccountabilityService accountabilityService;
  private final ObjectMapper objectMapper;

  public UserDeletedConsumer(AccountabilityService accountabilityService, ObjectMapper objectMapper) {
    this.accountabilityService = accountabilityService;
    this.objectMapper = objectMapper;
  }

  @SqsListener("accountability-user-deleted-queue")
  public void onUserDeleted(String rawMessage) throws JsonProcessingException {
    accountabilityService.purgeUser(objectMapper.readValue(rawMessage, EVENT_TYPE).payload().userId());
  }
}

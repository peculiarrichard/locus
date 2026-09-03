package com.locus.distraction.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.locus.distraction.repository.DistractionEventRepository;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// Consumes UserDeleted to purge this user's distraction history, per technical-spec.md §9's
// erasure cascade. Deserializes the raw body manually via TypeReference: Spring Cloud AWS's SQS
// message conversion resolves the @SqsListener parameter type by erasure, so a generic
// EventEnvelope<T> parameter arrives with payload as a raw LinkedHashMap instead of T.
@Component
public class UserDeletedConsumer {

  private static final TypeReference<EventEnvelope<UserDeletedPayload>> EVENT_TYPE = new TypeReference<>() {
  };

  private final DistractionEventRepository distractionEventRepository;
  private final ObjectMapper objectMapper;

  public UserDeletedConsumer(DistractionEventRepository distractionEventRepository, ObjectMapper objectMapper) {
    this.distractionEventRepository = distractionEventRepository;
    this.objectMapper = objectMapper;
  }

  @SqsListener("distraction-user-deleted-queue")
  @Transactional
  public void onUserDeleted(String rawMessage) throws JsonProcessingException {
    EventEnvelope<UserDeletedPayload> event = objectMapper.readValue(rawMessage, EVENT_TYPE);
    distractionEventRepository.deleteByUserId(event.payload().userId());
  }
}

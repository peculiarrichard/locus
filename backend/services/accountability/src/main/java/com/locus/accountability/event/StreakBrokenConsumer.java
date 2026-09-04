package com.locus.accountability.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.locus.accountability.domain.AccountabilityMember;
import com.locus.accountability.domain.MemberStatus;
import com.locus.accountability.repository.AccountabilityMemberRepository;
import io.awspring.cloud.sqs.annotation.SqsListener;
import java.time.Instant;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// Relays Analytics Service's own authoritative StreakBroken signal to the rest of any group this
// user is an active member of, per frd.md — Accountability Service doesn't run its own
// break-detection, it consumes Analytics' instead. Idempotency-guarded: republishing
// AccountabilityPartnerActivityUpdate is not naturally idempotent under SQS's at-least-once
// redelivery — see IdempotencyGuard.
@Component
public class StreakBrokenConsumer {

  private static final TypeReference<EventEnvelope<StreakBrokenPayload>> EVENT_TYPE = new TypeReference<>() {
  };

  private final AccountabilityMemberRepository memberRepository;
  private final EventPublisher eventPublisher;
  private final IdempotencyGuard idempotencyGuard;
  private final ObjectMapper objectMapper;

  public StreakBrokenConsumer(
      AccountabilityMemberRepository memberRepository,
      EventPublisher eventPublisher,
      IdempotencyGuard idempotencyGuard,
      ObjectMapper objectMapper) {
    this.memberRepository = memberRepository;
    this.eventPublisher = eventPublisher;
    this.idempotencyGuard = idempotencyGuard;
    this.objectMapper = objectMapper;
  }

  @SqsListener("accountability-streak-broken-queue")
  public void onStreakBroken(String rawMessage) throws JsonProcessingException {
    EventEnvelope<StreakBrokenPayload> event = objectMapper.readValue(rawMessage, EVENT_TYPE);
    if (!idempotencyGuard.claim(event.eventId())) {
      return;
    }
    relay(event.payload());
  }

  @Transactional(readOnly = true)
  void relay(StreakBrokenPayload payload) {
    for (AccountabilityMember membership : memberRepository.findByUserIdAndStatus(payload.userId(),
        MemberStatus.ACTIVE)) {
      for (AccountabilityMember other : memberRepository.findByGroupIdAndStatus(membership.getGroupId(),
          MemberStatus.ACTIVE)) {
        if (!other.getUserId().equals(payload.userId())) {
          eventPublisher.publishPartnerActivityUpdate(
              new AccountabilityPartnerActivityUpdatePayload(
                  other.getUserId(), payload.userId(), "streak_broken", Instant.now()));
        }
      }
    }
  }
}

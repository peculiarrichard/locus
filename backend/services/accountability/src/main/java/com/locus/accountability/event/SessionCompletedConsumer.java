package com.locus.accountability.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.locus.accountability.domain.AccountabilityMember;
import com.locus.accountability.domain.MemberActivityCompletion;
import com.locus.accountability.domain.MemberStatus;
import com.locus.accountability.repository.AccountabilityMemberRepository;
import com.locus.accountability.repository.MemberActivityCompletionRepository;
import io.awspring.cloud.sqs.annotation.SqsListener;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// Consumes SessionCompleted to derive daily completion and notify the rest of any group this
// user is an active member of, per frd.md. Writes nothing for a user who isn't in any group —
// see frd.md's independence guarantee. Idempotency-guarded: this also republishes
// AccountabilityPartnerActivityUpdate, which is not naturally idempotent under SQS's
// at-least-once redelivery — see IdempotencyGuard.
@Component
public class SessionCompletedConsumer {

  private static final TypeReference<EventEnvelope<SessionCompletedPayload>> EVENT_TYPE = new TypeReference<>() {
  };

  private final AccountabilityMemberRepository memberRepository;
  private final MemberActivityCompletionRepository completionRepository;
  private final EventPublisher eventPublisher;
  private final IdempotencyGuard idempotencyGuard;
  private final ObjectMapper objectMapper;

  public SessionCompletedConsumer(
      AccountabilityMemberRepository memberRepository,
      MemberActivityCompletionRepository completionRepository,
      EventPublisher eventPublisher,
      IdempotencyGuard idempotencyGuard,
      ObjectMapper objectMapper) {
    this.memberRepository = memberRepository;
    this.completionRepository = completionRepository;
    this.eventPublisher = eventPublisher;
    this.idempotencyGuard = idempotencyGuard;
    this.objectMapper = objectMapper;
  }

  @SqsListener("accountability-session-completed-queue")
  public void onSessionCompleted(String rawMessage) throws JsonProcessingException {
    EventEnvelope<SessionCompletedPayload> event = objectMapper.readValue(rawMessage, EVENT_TYPE);
    if (!idempotencyGuard.claim(event.eventId())) {
      return;
    }
    process(event.payload());
  }

  @Transactional
  void process(SessionCompletedPayload payload) {
    List<AccountabilityMember> memberships = memberRepository.findByUserIdAndStatus(payload.userId(),
        MemberStatus.ACTIVE);
    if (memberships.isEmpty()) {
      return;
    }

    LocalDate day = payload.completedAt().atZone(ZoneOffset.UTC).toLocalDate();
    if (!completionRepository.existsByUserIdAndCompletedDate(payload.userId(), day)) {
      completionRepository.save(new MemberActivityCompletion(payload.userId(), day));
    }

    for (AccountabilityMember membership : memberships) {
      for (AccountabilityMember other : memberRepository.findByGroupIdAndStatus(membership.getGroupId(),
          MemberStatus.ACTIVE)) {
        if (!other.getUserId().equals(payload.userId())) {
          eventPublisher.publishPartnerActivityUpdate(
              new AccountabilityPartnerActivityUpdatePayload(
                  other.getUserId(), payload.userId(), "session_completed", Instant.now()));
        }
      }
    }
  }
}

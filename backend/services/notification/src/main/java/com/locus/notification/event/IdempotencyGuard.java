package com.locus.notification.event;

import com.locus.notification.domain.ProcessedEventId;
import com.locus.notification.repository.ProcessedEventIdRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

// Applied by every consumer in this service, per frd.md — SQS is at-least-once delivery, and
// unlike Distraction Logging's client-generated id or Session's DB constraint, none of the
// events this service consumes carry their own natural dedupe key.
@Component
public class IdempotencyGuard {

  private final ProcessedEventIdRepository processedEventIdRepository;

  public IdempotencyGuard(ProcessedEventIdRepository processedEventIdRepository) {
    this.processedEventIdRepository = processedEventIdRepository;
  }

  // REQUIRES_NEW so the claim commits (or the conflict is observed) independently of the
  // caller's own transaction, and a duplicate-claim race is a normal outcome, not an error.
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public boolean claim(String eventId) {
    if (processedEventIdRepository.existsById(eventId)) {
      return false;
    }
    try {
      processedEventIdRepository.save(new ProcessedEventId(eventId));
      return true;
    } catch (DataIntegrityViolationException e) {
      return false;
    }
  }
}

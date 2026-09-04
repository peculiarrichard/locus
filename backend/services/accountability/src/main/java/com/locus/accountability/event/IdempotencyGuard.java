package com.locus.accountability.event;

import com.locus.accountability.domain.ProcessedEventId;
import com.locus.accountability.repository.ProcessedEventIdRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

// Applied to consumers whose effect isn't naturally idempotent under SQS's at-least-once
// redelivery — see V2__idempotency.sql. A DataIntegrityViolationException here means a
// concurrent claim race, which is a normal outcome (skip), not an error.
@Component
public class IdempotencyGuard {

  private final ProcessedEventIdRepository processedEventIdRepository;

  public IdempotencyGuard(ProcessedEventIdRepository processedEventIdRepository) {
    this.processedEventIdRepository = processedEventIdRepository;
  }

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

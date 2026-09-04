package com.locus.analytics.event;

import org.springframework.dao.DataIntegrityViolationException;

// Retries a transactional ingest call once after a unique-constraint race. A concurrent "load
// the shared daily/hourly row, increment it, save" collision between two SQS listener threads
// processing events for the same user on the same day is expected here, not exceptional, given
// Spring Cloud AWS's default listener concurrency — e.g. a user completing several sessions in
// quick succession. The retry can't happen inside the failed @Transactional method itself:
// Postgres aborts the rest of a transaction after any statement error, so it must be a fresh
// call back into the (proxied) service, starting a brand-new transaction.
final class ConflictRetry {

  private ConflictRetry() {
  }

  static void run(Runnable action) {
    try {
      action.run();
    } catch (DataIntegrityViolationException e) {
      action.run();
    }
  }
}

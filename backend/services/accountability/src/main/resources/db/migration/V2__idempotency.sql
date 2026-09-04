-- Found during Phase 11's cross-service idempotency verification pass: SessionCompletedConsumer
-- and StreakBrokenConsumer both unconditionally republish AccountabilityPartnerActivityUpdate,
-- which is not naturally idempotent under SQS's at-least-once redelivery — a redelivered event
-- would send a duplicate notification. See code-implementation-logs.md.
CREATE TABLE processed_event_ids (
    event_id VARCHAR(64) PRIMARY KEY,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

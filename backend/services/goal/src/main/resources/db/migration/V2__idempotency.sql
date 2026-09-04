-- Found during Phase 11's cross-service idempotency verification pass: SessionCompletedConsumer's
-- rollup (increment total_sessions/total_duration_seconds) is not naturally idempotent under
-- SQS's at-least-once redelivery — a redelivered event would double-count. See
-- code-implementation-logs.md.
CREATE TABLE processed_event_ids (
    event_id VARCHAR(64) PRIMARY KEY,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

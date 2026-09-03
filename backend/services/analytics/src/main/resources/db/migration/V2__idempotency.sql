-- Found during Phase 11's cross-service idempotency verification pass: the session/distraction
-- ingestion consumers increment shared daily/hourly counters, which is not naturally idempotent
-- under SQS's at-least-once redelivery — a redelivered event would double-count. See
-- code-implementation-logs.md.
CREATE TABLE processed_event_ids (
    event_id VARCHAR(64) PRIMARY KEY,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE user_contacts (
    user_id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL,
    display_name VARCHAR(200),
    timezone VARCHAR(64) NOT NULL DEFAULT 'Africa/Lagos',
    reminder_time TIME,
    bounced BOOLEAN NOT NULL DEFAULT false,
    -- Guards the per-user reminder job against sending twice in one day if a run overlaps the
    -- exact reminder minute more than once (job jitter, restart, etc.) — not in frd.md's literal
    -- table but required for the job's own "once per day" contract to actually hold.
    last_reminder_sent_date DATE,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE last_session_activity (
    user_id UUID PRIMARY KEY,
    last_completed_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE processed_event_ids (
    event_id VARCHAR(64) PRIMARY KEY,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE notification_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    notification_type VARCHAR(64) NOT NULL,
    sent_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_notification_log_user_id ON notification_log(user_id);

-- Not in frd.md's literal 4-table list. frd.md's Notification section says WeeklySummaryDue
-- "includes batched partner-activity" but WeeklySummaryDue's own payload (already locked and
-- built in Analytics Service, Phase 8) carries no partner-activity content — Analytics Service
-- has no visibility into Accountability data at all. Resolved by having Notification Service do
-- its own local correlation: session_completed-type AccountabilityPartnerActivityUpdate events
-- accumulate here instead of being sent immediately, then get folded into the weekly summary
-- email and cleared when WeeklySummaryDue arrives for that user.
CREATE TABLE pending_partner_activity (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    partner_user_id UUID NOT NULL,
    activity_type VARCHAR(32) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_pending_partner_activity_user_id ON pending_partner_activity(user_id);

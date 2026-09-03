CREATE TABLE distraction_events (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    session_id UUID NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    duration_seconds INT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_distraction_events_user_session ON distraction_events(user_id, session_id);

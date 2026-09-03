CREATE TABLE sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    session_type VARCHAR(32) NOT NULL,
    planned_duration_seconds INT,
    goal_id UUID,
    work_minutes INT,
    break_minutes INT,
    cycle_count INT,
    started_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    accumulated_pause_seconds INT NOT NULL DEFAULT 0,
    paused_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    abandoned_at TIMESTAMPTZ,
    duration_seconds INT,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE'
);

CREATE INDEX idx_sessions_user_id ON sessions(user_id);

-- Enforces "at most one non-terminal session per user" at the database level, per frd.md.
CREATE UNIQUE INDEX idx_sessions_one_active_per_user ON sessions(user_id)
    WHERE status IN ('ACTIVE', 'PAUSED');

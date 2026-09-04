CREATE TABLE session_stats_daily (
    user_id UUID NOT NULL,
    stat_date DATE NOT NULL,
    sessions_completed INT NOT NULL DEFAULT 0,
    sessions_abandoned INT NOT NULL DEFAULT 0,
    total_focus_seconds BIGINT NOT NULL DEFAULT 0,
    -- Day-scoped distraction count, kept separately from distraction_stats_hourly (deliberately
    -- all-time, per frd.md) so WeeklySummaryDue's distraction_count field is computable at all.
    distraction_count INT NOT NULL DEFAULT 0,
    -- Whether at least one individual completed session this day met the streak's minimum
    -- duration floor. Kept alongside the daily aggregates because a day's *total* focus time
    -- can't distinguish "one long qualifying session" from "several short non-qualifying ones,"
    -- and the streak rule (frd.md) is explicitly per-session, not per-day-total.
    has_qualifying_session BOOLEAN NOT NULL DEFAULT false,
    PRIMARY KEY (user_id, stat_date)
);

-- Not in frd.md's literal 4-table list — added because "best study hours ... computed over a
-- rolling 90-day window" (frd.md) needs date-level granularity that a pure hour-of-day bucket
-- can't provide; distraction_stats_hourly below is deliberately left as frd.md specified it
-- (all-time, no date column) since only best-hours has an explicit rolling-window requirement.
CREATE TABLE session_stats_hourly (
    user_id UUID NOT NULL,
    stat_date DATE NOT NULL,
    hour_of_day SMALLINT NOT NULL,
    focus_seconds BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id, stat_date, hour_of_day)
);

CREATE TABLE distraction_stats_hourly (
    user_id UUID NOT NULL,
    hour_of_day SMALLINT NOT NULL,
    distraction_count INT NOT NULL DEFAULT 0,
    total_focus_seconds_in_bucket BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id, hour_of_day)
);

CREATE TABLE streaks (
    user_id UUID PRIMARY KEY,
    current_streak_days INT NOT NULL DEFAULT 0,
    longest_streak_days INT NOT NULL DEFAULT 0,
    last_qualifying_day DATE
);

CREATE TABLE user_locale (
    user_id UUID PRIMARY KEY,
    timezone VARCHAR(64) NOT NULL DEFAULT 'Africa/Lagos'
);

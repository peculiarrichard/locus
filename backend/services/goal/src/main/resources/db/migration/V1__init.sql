CREATE TABLE goals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    goal_type VARCHAR(32) NOT NULL,
    title VARCHAR(200) NOT NULL,
    target_date DATE NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_goals_user_id ON goals(user_id);

-- Static, non-user-editable seed data. Copied into goal_milestones on goal creation, never
-- referenced live afterward, per frd.md's copy-on-create requirement.
CREATE TABLE plan_templates (
    id BIGSERIAL PRIMARY KEY,
    goal_type VARCHAR(32) NOT NULL,
    milestone_offset_days INT NOT NULL,
    milestone_name VARCHAR(200) NOT NULL,
    description VARCHAR(500)
);

CREATE TABLE goal_milestones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    goal_id UUID NOT NULL REFERENCES goals(id) ON DELETE CASCADE,
    milestone_name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    -- Retained from the template so an edited target_date can recompute due_date proportionally,
    -- per frd.md's edit-recomputes-incomplete-milestones edge case.
    milestone_offset_days INT NOT NULL,
    due_date DATE NOT NULL,
    completed_at TIMESTAMPTZ
);

CREATE INDEX idx_goal_milestones_goal_id ON goal_milestones(goal_id);

CREATE TABLE goal_session_activity (
    goal_id UUID PRIMARY KEY REFERENCES goals(id) ON DELETE CASCADE,
    total_sessions INT NOT NULL DEFAULT 0,
    total_duration_seconds BIGINT NOT NULL DEFAULT 0
);

-- Tracks which of the 30/14/7/1-day GoalDeadlineApproaching thresholds have already fired for a
-- goal, per frd.md's "a set, not a single flag" requirement — makes the daily scan robust to a
-- skipped day rather than relying on exact day-count equality.
CREATE TABLE goal_deadline_notifications (
    goal_id UUID NOT NULL REFERENCES goals(id) ON DELETE CASCADE,
    threshold_days INT NOT NULL,
    PRIMARY KEY (goal_id, threshold_days)
);

INSERT INTO plan_templates (goal_type, milestone_offset_days, milestone_name, description) VALUES
    ('EXAM', 56, 'Diagnostic review', 'Assess current knowledge and identify weak areas'),
    ('EXAM', 28, 'Core content review', 'Work through the full syllabus systematically'),
    ('EXAM', 14, 'Practice tests', 'Timed practice under exam conditions'),
    ('EXAM', 7, 'Final review pass', 'Revisit weak areas found during practice tests'),
    ('EXAM', 1, 'Final prep and rest', 'Light review only, prioritize rest before the exam'),
    ('CERTIFICATION', 60, 'Review exam blueprint', 'Read the official objectives/blueprint end to end'),
    ('CERTIFICATION', 30, 'Complete core study modules', 'Work through the primary study material'),
    ('CERTIFICATION', 14, 'Take practice exams', 'Identify remaining gaps via practice scoring'),
    ('CERTIFICATION', 7, 'Review weak areas', 'Focus remaining study time on practice-exam gaps'),
    ('CERTIFICATION', 2, 'Final review', 'Light review only, confirm exam logistics'),
    ('CAPSTONE', 90, 'Define project scope', 'Lock down scope, requirements, and success criteria'),
    ('CAPSTONE', 60, 'Complete research and planning', 'Finish background research and technical planning'),
    ('CAPSTONE', 30, 'First draft or prototype complete', 'Have an end-to-end first pass, even if rough'),
    ('CAPSTONE', 14, 'Incorporate feedback', 'Revise based on review feedback'),
    ('CAPSTONE', 3, 'Final polish and submission prep', 'Final proofreading/testing and submission logistics');

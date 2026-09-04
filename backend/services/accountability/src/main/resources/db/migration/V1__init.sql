CREATE TABLE accountability_groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_type VARCHAR(16) NOT NULL,
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- status is ACTIVE or LEFT only — the "invited" state from frd.md's literal enum lives in
-- accountability_invites below instead, since an invite needs a shareable code before anyone
-- has joined, which doesn't map cleanly onto a per-member row.
CREATE TABLE accountability_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id UUID NOT NULL REFERENCES accountability_groups(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    joined_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    left_at TIMESTAMPTZ,
    UNIQUE (group_id, user_id)
);

CREATE INDEX idx_accountability_members_user_id ON accountability_members(user_id);
CREATE INDEX idx_accountability_members_group_id ON accountability_members(group_id);

CREATE TABLE accountability_invites (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(32) NOT NULL UNIQUE,
    group_id UUID NOT NULL REFERENCES accountability_groups(id) ON DELETE CASCADE,
    created_by UUID NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at TIMESTAMPTZ NOT NULL
);

-- Only written for users who are an active member of at least one group at the moment their
-- SessionCompleted event is consumed, per frd.md's independence guarantee: a user who never
-- pairs has zero rows here, ever.
CREATE TABLE member_activity_completions (
    user_id UUID NOT NULL,
    completed_date DATE NOT NULL,
    PRIMARY KEY (user_id, completed_date)
);

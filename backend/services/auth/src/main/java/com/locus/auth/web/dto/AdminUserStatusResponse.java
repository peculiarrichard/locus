package com.locus.auth.web.dto;

import java.time.Instant;
import java.util.UUID;

// Response for GET /admin/users/{id} — read-only account status view for support/admin operators.
public record AdminUserStatusResponse(UUID id, String email, String status, boolean emailVerified, boolean mfaEnabled,
    int failedLoginCount, Instant lockedUntil) {
}

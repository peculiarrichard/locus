package com.locus.auth.web.dto;

import java.util.UUID;

// Response for GET /users/me.
public record ProfileResponse(UUID id, String email, String displayName, String timezone, boolean emailVerified,
    boolean mfaEnabled) {
}

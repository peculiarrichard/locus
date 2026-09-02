package com.locus.auth.web.dto;

import java.time.Instant;
import java.util.UUID;

// One row in GET /users/me/devices — a device's active refresh-token session.
public record DeviceResponse(UUID id, String deviceLabel, Instant createdAt, Instant expiresAt) {
}

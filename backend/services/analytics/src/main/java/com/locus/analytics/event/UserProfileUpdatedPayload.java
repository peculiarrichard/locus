package com.locus.analytics.event;

import java.util.UUID;

// Full-snapshot payload per technical-spec.md §7 — this service only reads timezone from it.
public record UserProfileUpdatedPayload(UUID userId, String email, String displayName, String timezone) {
}

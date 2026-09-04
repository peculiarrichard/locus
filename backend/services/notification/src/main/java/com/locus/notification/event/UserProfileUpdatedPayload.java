package com.locus.notification.event;

import java.util.UUID;

// Full-snapshot payload per technical-spec.md §7.
public record UserProfileUpdatedPayload(UUID userId, String email, String displayName, String timezone) {
}

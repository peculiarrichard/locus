package com.locus.auth.event;

import java.util.UUID;

// Payload for the UserProfileUpdated event — a full snapshot on any profile field change, per technical-spec.md §7/§9.
public record UserProfileUpdatedPayload(UUID userId, String email, String displayName, String timezone) {
}

package com.locus.auth.event;

import java.util.UUID;

// Payload for the PasswordResetRequested event — carries the raw reset token, a deliberate named
// exception per technical-spec.md §7.
public record PasswordResetRequestedPayload(UUID userId, String email, String resetToken) {
}

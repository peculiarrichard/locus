package com.locus.auth.event;

import java.util.UUID;

// Payload for the UserRegistered event. Carries the raw verification token alongside the same
// deliberate-exception reasoning technical-spec.md §7 already applies to PasswordResetRequested's
// reset token — Notification Service has no other way to learn it, and republished on resend too.
public record UserRegisteredPayload(UUID userId, String email, String displayName, String verificationToken) {
}

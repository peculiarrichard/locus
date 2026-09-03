package com.locus.notification.event;

import java.util.UUID;

public record PasswordResetRequestedPayload(UUID userId, String email, String resetToken) {
}

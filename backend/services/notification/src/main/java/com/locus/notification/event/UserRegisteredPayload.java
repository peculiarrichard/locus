package com.locus.notification.event;

import java.util.UUID;

public record UserRegisteredPayload(UUID userId, String email, String displayName, String verificationToken) {
}

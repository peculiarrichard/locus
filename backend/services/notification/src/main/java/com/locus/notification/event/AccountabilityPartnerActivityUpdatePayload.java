package com.locus.notification.event;

import java.time.Instant;
import java.util.UUID;

public record AccountabilityPartnerActivityUpdatePayload(
    UUID userId, UUID partnerUserId, String activityType, Instant occurredAt) {
}

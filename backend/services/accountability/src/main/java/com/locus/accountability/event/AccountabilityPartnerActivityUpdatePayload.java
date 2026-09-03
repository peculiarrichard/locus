package com.locus.accountability.event;

import java.time.Instant;
import java.util.UUID;

// activity_type: "session_completed" | "streak_broken" | "member_joined" | "account_deleted"
// (the last one is this implementation's fulfillment of frd.md's "remaining members see 'partner
// account deleted'" requirement on pairing dissolution — not enumerated as a separate value in
// technical-spec.md's event catalog, added here since nothing else carries that notice).
public record AccountabilityPartnerActivityUpdatePayload(
    UUID userId, UUID partnerUserId, String activityType, Instant occurredAt) {
}

package com.locus.session.web.dto;

import java.time.Instant;

// Optional client-supplied timestamp for crash/relaunch reconciliation, per frd.md's orphaned-
// session edge case. Null means "abandon now."
public record AbandonSessionRequest(Instant abandonedAt) {
}

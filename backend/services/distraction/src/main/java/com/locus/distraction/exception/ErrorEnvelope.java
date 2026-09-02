package com.locus.distraction.exception;

import java.time.Instant;

// The standardized error envelope shape from technical-spec.md §2, reused at the service level
// so the Gateway can forward it unchanged.
public record ErrorEnvelope(String errorCode, String message, String correlationId, Instant timestamp) {
}

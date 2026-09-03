package com.locus.gateway.web;

import java.time.Instant;

// The standardized error envelope every error response uses, per technical-spec.md §2.
public record ErrorEnvelope(String errorCode, String message, String correlationId, Instant timestamp) {
}

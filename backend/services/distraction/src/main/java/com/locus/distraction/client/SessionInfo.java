package com.locus.distraction.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;

// The subset of Session Service's SessionResponse this service needs for occurred_at validation.
@JsonIgnoreProperties(ignoreUnknown = true)
public record SessionInfo(Instant startedAt, Instant completedAt, String status) {
}

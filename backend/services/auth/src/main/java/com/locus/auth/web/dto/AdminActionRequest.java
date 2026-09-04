package com.locus.auth.web.dto;

import jakarta.validation.constraints.NotBlank;

// Request body for every admin action endpoint — a reason is mandatory for the audit log, per frd.md.
public record AdminActionRequest(@NotBlank String reason) {
}

package com.locus.auth.web.dto;

import jakarta.validation.constraints.Size;

// Request body for PATCH /users/me — both fields optional, only supplied ones change. A real gap
// found during Phase 12's security review: displayName had no length bound, and timezone wasn't
// validated as a real IANA zone id at all — see ValidTimezoneValidator's own comment for why that
// specifically matters here (it isn't just cosmetic).
public record ProfileUpdateRequest(@Size(max = 255) String displayName, @ValidTimezone String timezone) {
}

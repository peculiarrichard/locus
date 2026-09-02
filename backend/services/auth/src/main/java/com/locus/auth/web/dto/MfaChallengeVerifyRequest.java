package com.locus.auth.web.dto;

import jakarta.validation.constraints.NotBlank;

// Request body for POST /auth/mfa/challenge.
public record MfaChallengeVerifyRequest(@NotBlank String mfaChallengeToken, @NotBlank String code) {
}

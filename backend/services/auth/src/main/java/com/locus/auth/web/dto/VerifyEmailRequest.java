package com.locus.auth.web.dto;

import jakarta.validation.constraints.NotBlank;

// Request body for POST /auth/verify-email.
public record VerifyEmailRequest(@NotBlank String token) {
}

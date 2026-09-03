package com.locus.auth.web.dto;

import jakarta.validation.constraints.NotBlank;

// Request body for POST /auth/mfa/confirm.
public record MfaConfirmRequest(@NotBlank String code) {
}

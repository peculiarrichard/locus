package com.locus.auth.web.dto;

import jakarta.validation.constraints.NotBlank;

// Request body for POST /auth/password-reset/confirm.
public record PasswordResetConfirmRequest(@NotBlank String token, @NotBlank String newPassword) {
}

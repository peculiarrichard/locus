package com.locus.auth.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Request body for POST /auth/password-reset/request.
public record PasswordResetRequestRequest(@NotBlank @Email String email) {
}

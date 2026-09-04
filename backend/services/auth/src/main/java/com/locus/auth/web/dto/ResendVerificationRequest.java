package com.locus.auth.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Request body for POST /auth/verify-email/resend.
public record ResendVerificationRequest(@NotBlank @Email String email) {
}

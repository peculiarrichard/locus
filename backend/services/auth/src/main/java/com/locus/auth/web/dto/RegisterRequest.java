package com.locus.auth.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Request body for POST /auth/register.
public record RegisterRequest(@NotBlank @Email String email, @NotBlank String password) {
}

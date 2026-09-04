package com.locus.auth.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Request body for POST /auth/login.
public record LoginRequest(@NotBlank @Email String email, @NotBlank String password, String deviceLabel) {
}

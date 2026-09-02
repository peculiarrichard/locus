package com.locus.auth.web.dto;

import jakarta.validation.constraints.NotBlank;

// Request body for POST /auth/refresh.
public record RefreshRequest(@NotBlank String refreshToken) {
}

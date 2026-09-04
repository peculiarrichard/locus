package com.locus.auth.web.dto;

// Response shape for any endpoint that issues a fresh token pair.
public record TokenResponse(String accessToken, String refreshToken) {
}

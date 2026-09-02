package com.locus.auth.web.dto;

// Response for POST /auth/mfa/enroll — the client renders this as a QR code.
public record MfaEnrollResponse(String otpAuthUri) {
}

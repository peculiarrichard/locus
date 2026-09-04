package com.locus.auth.web.dto;

import jakarta.validation.constraints.NotBlank;

// Request body for any action requiring re-authentication rather than just an active session:
// MFA disable, account deletion.
public record ReauthRequest(@NotBlank String password) {
}

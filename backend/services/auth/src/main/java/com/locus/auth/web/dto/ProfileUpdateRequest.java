package com.locus.auth.web.dto;

// Request body for PATCH /users/me — both fields optional, only supplied ones change.
public record ProfileUpdateRequest(String displayName, String timezone) {
}

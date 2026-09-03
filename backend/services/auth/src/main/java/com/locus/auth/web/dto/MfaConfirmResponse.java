package com.locus.auth.web.dto;

import java.util.List;

// Response for POST /auth/mfa/confirm — the 10 recovery codes, shown once, per frd.md.
public record MfaConfirmResponse(List<String> recoveryCodes) {
}

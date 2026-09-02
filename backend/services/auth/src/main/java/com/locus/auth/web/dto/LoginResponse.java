package com.locus.auth.web.dto;

// Response for POST /auth/login — either the token pair, or (when MFA is enabled) only an mfaChallengeToken.
public record LoginResponse(String accessToken, String refreshToken, String mfaChallengeToken) {

  public static LoginResponse tokens(String accessToken, String refreshToken) {
    return new LoginResponse(accessToken, refreshToken, null);
  }

  public static LoginResponse mfaRequired(String mfaChallengeToken) {
    return new LoginResponse(null, null, mfaChallengeToken);
  }
}

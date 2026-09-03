package com.locus.auth.security;

import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.stereotype.Component;

// Generates high-entropy, URL-safe opaque tokens for refresh/verification/reset flows.
@Component
public class RandomTokenGenerator {

  private final SecureRandom secureRandom = new SecureRandom();

  public String generate() {
    byte[] bytes = new byte[32];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}

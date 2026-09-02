package com.locus.auth.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

// Unit tests for the SHA-256 opaque-token hasher used for refresh/verification/reset tokens at rest.
class TokenHasherTest {

  private final TokenHasher hasher = new TokenHasher();

  @Test
  void sameInputProducesSameHash() {
    assertThat(hasher.hash("abc123")).isEqualTo(hasher.hash("abc123"));
  }

  @Test
  void differentInputProducesDifferentHash() {
    assertThat(hasher.hash("abc123")).isNotEqualTo(hasher.hash("abc124"));
  }

  @Test
  void hashIsNotThePlaintext() {
    assertThat(hasher.hash("abc123")).isNotEqualTo("abc123");
  }
}

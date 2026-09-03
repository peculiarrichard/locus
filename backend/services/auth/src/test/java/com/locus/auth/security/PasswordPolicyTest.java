package com.locus.auth.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

// Unit tests for the locked password policy (prerequisites.md): 8-16 chars, letter+number+special, no "password".
class PasswordPolicyTest {

  private final PasswordPolicy policy = new PasswordPolicy();

  @Test
  void acceptsAValidPassword() {
    assertThat(policy.isValid("Testpass1!")).isTrue();
  }

  @Test
  void rejectsTooShort() {
    assertThat(policy.isValid("Ab1!")).isFalse();
  }

  @Test
  void rejectsTooLong() {
    assertThat(policy.isValid("Abcdefghijklmnop1!")).isFalse();
  }

  @Test
  void rejectsMissingNumber() {
    assertThat(policy.isValid("Abcdefgh!")).isFalse();
  }

  @Test
  void rejectsMissingSpecialCharacter() {
    assertThat(policy.isValid("Abcdefgh1")).isFalse();
  }

  @Test
  void rejectsMissingLetter() {
    assertThat(policy.isValid("12345678!")).isFalse();
  }

  @Test
  void rejectsThePasswordSubstringCaseInsensitive() {
    assertThat(policy.isValid("MyPassword1!")).isFalse();
    assertThat(policy.isValid("myPASSWORD1!")).isFalse();
  }

  @Test
  void rejectsNull() {
    assertThat(policy.isValid(null)).isFalse();
  }
}

package com.locus.accountability.service;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

// Short, shareable, URL-safe invite codes — not cryptographically sensitive the way an auth
// token is (a guessed code only lets someone join a study group), so a shorter alphabet/length
// than Auth Service's token generator is an intentional, proportionate choice.
@Component
public class InviteCodeGenerator {

  private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
  private static final int LENGTH = 8;
  private final SecureRandom random = new SecureRandom();

  public String generate() {
    StringBuilder code = new StringBuilder(LENGTH);
    for (int i = 0; i < LENGTH; i++) {
      code.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
    }
    return code.toString();
  }
}

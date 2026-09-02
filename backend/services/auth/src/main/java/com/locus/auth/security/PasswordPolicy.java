package com.locus.auth.security;

import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

// Enforces the locked password policy: 8-16 chars, >=1 letter/number/special char, no "password" substring.
@Component
public class PasswordPolicy {

  private static final int MIN_LENGTH = 8;
  private static final int MAX_LENGTH = 16;
  private static final Pattern LETTER = Pattern.compile("[A-Za-z]");
  private static final Pattern NUMBER = Pattern.compile("[0-9]");
  private static final Pattern SPECIAL = Pattern.compile("[^A-Za-z0-9]");

  public boolean isValid(String password) {
    if (password == null || password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
      return false;
    }
    if (password.toLowerCase().contains("password")) {
      return false;
    }
    return LETTER.matcher(password).find() && NUMBER.matcher(password).find() && SPECIAL.matcher(password).find();
  }
}

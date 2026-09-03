package com.locus.auth.web.dto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.DateTimeException;
import java.time.ZoneId;

public class ValidTimezoneValidator implements ConstraintValidator<ValidTimezone, String> {

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }
    try {
      ZoneId.of(value);
      return true;
    } catch (DateTimeException e) {
      return false;
    }
  }
}

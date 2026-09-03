package com.locus.auth.exception;

import java.time.Instant;
import org.springframework.http.HttpStatus;

// Base exception carrying the HTTP status and errorCode for the standardized error envelope.
public class ApiException extends RuntimeException {

  private final HttpStatus status;
  private final String errorCode;

  public ApiException(HttpStatus status, String errorCode, String message) {
    super(message);
    this.status = status;
    this.errorCode = errorCode;
  }

  public static ApiException invalidCredentials() {
    return new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid email or password");
  }

  public static ApiException accountLocked(Instant until) {
    return new ApiException(HttpStatus.FORBIDDEN, "ACCOUNT_LOCKED", "Account is locked until " + until);
  }

  public static ApiException emailNotVerified() {
    return new ApiException(HttpStatus.FORBIDDEN, "EMAIL_NOT_VERIFIED", "Email address is not verified");
  }

  public static ApiException emailAlreadyRegistered() {
    return new ApiException(HttpStatus.CONFLICT, "EMAIL_ALREADY_REGISTERED",
        "An account with this email already exists");
  }

  public static ApiException weakPassword() {
    return new ApiException(HttpStatus.BAD_REQUEST, "WEAK_PASSWORD", "Password does not meet the required policy");
  }

  public static ApiException invalidToken(String what) {
    return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_TOKEN", "Invalid or expired " + what);
  }

  public static ApiException rateLimited(String what) {
    return new ApiException(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMITED", what + " was requested too recently");
  }

  public static ApiException notFound(String what) {
    return new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", what + " not found");
  }

  public static ApiException conflict(String errorCode, String message) {
    return new ApiException(HttpStatus.CONFLICT, errorCode, message);
  }

  public static ApiException forbidden(String errorCode, String message) {
    return new ApiException(HttpStatus.FORBIDDEN, errorCode, message);
  }

  public HttpStatus getStatus() {
    return status;
  }

  public String getErrorCode() {
    return errorCode;
  }
}

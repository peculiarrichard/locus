package com.locus.analytics.exception;

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

  public static ApiException badRequest(String errorCode, String message) {
    return new ApiException(HttpStatus.BAD_REQUEST, errorCode, message);
  }

  public HttpStatus getStatus() {
    return status;
  }

  public String getErrorCode() {
    return errorCode;
  }
}

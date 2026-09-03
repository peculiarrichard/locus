package com.locus.session.exception;

import java.time.Instant;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Maps exceptions to the standardized error envelope every service uses, per technical-spec.md §2.
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ErrorEnvelope> handleApiException(ApiException e) {
    return ResponseEntity.status(e.getStatus()).body(envelope(e.getErrorCode(), e.getMessage()));
  }

  // The one-active-session-per-user database constraint (V1__init.sql) surfaces here as a
  // conflict, per frd.md's "409, not a silent no-op" requirement.
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorEnvelope> handleConflict(DataIntegrityViolationException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(envelope("SESSION_ALREADY_ACTIVE", "A non-terminal session already exists"));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorEnvelope> handleValidation(MethodArgumentNotValidException e) {
    String message = e.getBindingResult().getFieldErrors().stream()
        .findFirst()
        .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
        .orElse("Invalid request");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(envelope("VALIDATION_FAILED", message));
  }

  private ErrorEnvelope envelope(String errorCode, String message) {
    return new ErrorEnvelope(errorCode, message, UUID.randomUUID().toString(), Instant.now());
  }
}

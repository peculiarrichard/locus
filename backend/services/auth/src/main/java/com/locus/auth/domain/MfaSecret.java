package com.locus.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

// JPA entity for the mfa_secrets table — one encrypted TOTP secret per user, unconfirmed until
// the enrollment flow completes.
@Entity
@Table(name = "mfa_secrets")
public class MfaSecret {

  @Id
  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "encrypted_secret", nullable = false)
  private String encryptedSecret;

  @Column(nullable = false)
  private boolean confirmed = false;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "enabled_at")
  private Instant enabledAt;

  protected MfaSecret() {
  }

  public MfaSecret(UUID userId, String encryptedSecret) {
    this.userId = userId;
    this.encryptedSecret = encryptedSecret;
  }

  public UUID getUserId() {
    return userId;
  }

  public String getEncryptedSecret() {
    return encryptedSecret;
  }

  public boolean isConfirmed() {
    return confirmed;
  }

  public void setConfirmed(boolean confirmed) {
    this.confirmed = confirmed;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getEnabledAt() {
    return enabledAt;
  }

  public void setEnabledAt(Instant enabledAt) {
    this.enabledAt = enabledAt;
  }
}

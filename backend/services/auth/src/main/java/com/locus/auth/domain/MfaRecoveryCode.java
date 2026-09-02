package com.locus.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

// JPA entity for the mfa_recovery_codes table — one-time codes issued at MFA enrollment, hashed at rest.
@Entity
@Table(name = "mfa_recovery_codes")
public class MfaRecoveryCode {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "code_hash", nullable = false)
  private String codeHash;

  @Column(name = "used_at")
  private Instant usedAt;

  protected MfaRecoveryCode() {
  }

  public MfaRecoveryCode(UUID userId, String codeHash) {
    this.userId = userId;
    this.codeHash = codeHash;
  }

  public Long getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public String getCodeHash() {
    return codeHash;
  }

  public Instant getUsedAt() {
    return usedAt;
  }

  public void setUsedAt(Instant usedAt) {
    this.usedAt = usedAt;
  }
}

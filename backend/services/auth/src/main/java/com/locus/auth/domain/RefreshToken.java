package com.locus.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

// JPA entity for the refresh_tokens table — one row per device, rotated on every use, family_id
// ties a device's rotation chain together for theft detection.
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "token_hash", nullable = false, unique = true)
  private String tokenHash;

  @Column(name = "device_label")
  private String deviceLabel;

  @Column(name = "family_id", nullable = false)
  private UUID familyId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "revoked_at")
  private Instant revokedAt;

  protected RefreshToken() {
  }

  public RefreshToken(UUID userId, String tokenHash, String deviceLabel, UUID familyId, Instant expiresAt) {
    this.userId = userId;
    this.tokenHash = tokenHash;
    this.deviceLabel = deviceLabel;
    this.familyId = familyId;
    this.expiresAt = expiresAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public String getTokenHash() {
    return tokenHash;
  }

  public String getDeviceLabel() {
    return deviceLabel;
  }

  public UUID getFamilyId() {
    return familyId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public Instant getRevokedAt() {
    return revokedAt;
  }

  public void setRevokedAt(Instant revokedAt) {
    this.revokedAt = revokedAt;
  }

  public boolean isActive() {
    return revokedAt == null && expiresAt.isAfter(Instant.now());
  }
}

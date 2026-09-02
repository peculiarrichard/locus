package com.locus.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

// JPA entity for the admin_audit_log table — every admin action, exempt from the UserDeleted erasure cascade.
@Entity
@Table(name = "admin_audit_log")
public class AdminAuditLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "admin_user_id", nullable = false)
  private UUID adminUserId;

  @Column(name = "target_user_id", nullable = false)
  private UUID targetUserId;

  @Column(nullable = false)
  private String action;

  @Column(nullable = false)
  private String reason;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  protected AdminAuditLog() {
  }

  public AdminAuditLog(UUID adminUserId, UUID targetUserId, String action, String reason) {
    this.adminUserId = adminUserId;
    this.targetUserId = targetUserId;
    this.action = action;
    this.reason = reason;
  }

  public Long getId() {
    return id;
  }

  public UUID getAdminUserId() {
    return adminUserId;
  }

  public UUID getTargetUserId() {
    return targetUserId;
  }

  public String getAction() {
    return action;
  }

  public String getReason() {
    return reason;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}

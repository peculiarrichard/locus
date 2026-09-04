package com.locus.auth.repository;

import com.locus.auth.domain.AdminAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

// Spring Data repository for admin_audit_log.
public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLog, Long> {
}

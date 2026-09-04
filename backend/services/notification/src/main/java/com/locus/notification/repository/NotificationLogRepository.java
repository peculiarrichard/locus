package com.locus.notification.repository;

import com.locus.notification.domain.NotificationLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {
}

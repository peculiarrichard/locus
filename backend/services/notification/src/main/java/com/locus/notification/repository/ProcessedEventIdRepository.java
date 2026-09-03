package com.locus.notification.repository;

import com.locus.notification.domain.ProcessedEventId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventIdRepository extends JpaRepository<ProcessedEventId, String> {
}

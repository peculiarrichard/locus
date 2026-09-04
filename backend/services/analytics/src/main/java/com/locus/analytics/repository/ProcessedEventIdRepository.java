package com.locus.analytics.repository;

import com.locus.analytics.domain.ProcessedEventId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventIdRepository extends JpaRepository<ProcessedEventId, String> {
}

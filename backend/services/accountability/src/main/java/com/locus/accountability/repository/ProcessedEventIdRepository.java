package com.locus.accountability.repository;

import com.locus.accountability.domain.ProcessedEventId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventIdRepository extends JpaRepository<ProcessedEventId, String> {
}

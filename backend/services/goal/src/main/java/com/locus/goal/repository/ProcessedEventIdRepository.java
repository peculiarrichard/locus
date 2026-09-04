package com.locus.goal.repository;

import com.locus.goal.domain.ProcessedEventId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventIdRepository extends JpaRepository<ProcessedEventId, String> {
}

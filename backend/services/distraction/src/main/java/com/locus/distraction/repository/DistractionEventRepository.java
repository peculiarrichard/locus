package com.locus.distraction.repository;

import com.locus.distraction.domain.DistractionEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DistractionEventRepository extends JpaRepository<DistractionEvent, UUID> {

  List<DistractionEvent> findByUserIdAndSessionIdOrderByOccurredAt(UUID userId, UUID sessionId);

  void deleteByUserId(UUID userId);

  // Retention purge, per rules.md's 18-month T2 window (technical-spec.md §9).
  long deleteByOccurredAtBefore(Instant cutoff);
}

package com.locus.session.repository;

import com.locus.session.domain.Session;
import com.locus.session.domain.SessionStatus;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

// Spring Data repository for sessions.
public interface SessionRepository extends JpaRepository<Session, UUID> {
  List<Session> findByUserIdOrderByStartedAtDesc(UUID userId, Pageable pageable);

  List<Session> findByUserId(UUID userId);

  void deleteByUserId(UUID userId);

  // Retention purge, per rules.md's 18-month T2 window (technical-spec.md §9) — scoped to
  // terminal statuses only, so a pathologically long-running active/paused session is never
  // deleted purely on age.
  long deleteByStatusInAndStartedAtBefore(Collection<SessionStatus> terminalStatuses, Instant cutoff);
}

package com.locus.session.repository;

import com.locus.session.domain.Session;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

// Spring Data repository for sessions.
public interface SessionRepository extends JpaRepository<Session, UUID> {
  List<Session> findByUserIdOrderByStartedAtDesc(UUID userId, Pageable pageable);

  List<Session> findByUserId(UUID userId);

  void deleteByUserId(UUID userId);
}

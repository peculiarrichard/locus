package com.locus.analytics.repository;

import com.locus.analytics.domain.SessionStatsHourly;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionStatsHourlyRepository extends JpaRepository<SessionStatsHourly, SessionStatsHourly.Key> {

  List<SessionStatsHourly> findByUserIdAndStatDateAfter(UUID userId, LocalDate after);

  void deleteByUserId(UUID userId);
}

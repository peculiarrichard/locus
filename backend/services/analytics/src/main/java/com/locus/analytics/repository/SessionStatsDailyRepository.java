package com.locus.analytics.repository;

import com.locus.analytics.domain.SessionStatsDaily;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionStatsDailyRepository extends JpaRepository<SessionStatsDaily, SessionStatsDaily.Key> {

  List<SessionStatsDaily> findByUserIdAndStatDateBetween(UUID userId, LocalDate from, LocalDate to);

  void deleteByUserId(UUID userId);
}

package com.locus.analytics.repository;

import com.locus.analytics.domain.DistractionStatsHourly;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DistractionStatsHourlyRepository
    extends
      JpaRepository<DistractionStatsHourly, DistractionStatsHourly.Key> {

  List<DistractionStatsHourly> findByUserId(UUID userId);

  void deleteByUserId(UUID userId);
}

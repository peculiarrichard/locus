package com.locus.goal.repository;

import com.locus.goal.domain.Goal;
import com.locus.goal.domain.GoalStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoalRepository extends JpaRepository<Goal, UUID> {

  List<Goal> findByUserId(UUID userId);

  List<Goal> findByStatus(GoalStatus status);

  void deleteByUserId(UUID userId);
}

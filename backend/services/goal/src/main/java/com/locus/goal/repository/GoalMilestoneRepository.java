package com.locus.goal.repository;

import com.locus.goal.domain.GoalMilestone;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoalMilestoneRepository extends JpaRepository<GoalMilestone, UUID> {

  List<GoalMilestone> findByGoalId(UUID goalId);

  List<GoalMilestone> findByGoalIdAndCompletedAtIsNull(UUID goalId);
}

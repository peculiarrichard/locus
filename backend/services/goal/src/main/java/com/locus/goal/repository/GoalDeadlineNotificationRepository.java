package com.locus.goal.repository;

import com.locus.goal.domain.GoalDeadlineNotification;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoalDeadlineNotificationRepository
    extends
      JpaRepository<GoalDeadlineNotification, GoalDeadlineNotification.Key> {

  List<GoalDeadlineNotification> findByGoalId(UUID goalId);
}

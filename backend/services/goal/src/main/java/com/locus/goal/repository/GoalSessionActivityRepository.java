package com.locus.goal.repository;

import com.locus.goal.domain.GoalSessionActivity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoalSessionActivityRepository extends JpaRepository<GoalSessionActivity, UUID> {
}

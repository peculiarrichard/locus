package com.locus.goal.repository;

import com.locus.goal.domain.GoalType;
import com.locus.goal.domain.PlanTemplate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanTemplateRepository extends JpaRepository<PlanTemplate, Long> {

  List<PlanTemplate> findByGoalType(GoalType goalType);
}

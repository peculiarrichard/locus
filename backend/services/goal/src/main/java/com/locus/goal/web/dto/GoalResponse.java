package com.locus.goal.web.dto;

import com.locus.goal.domain.Goal;
import com.locus.goal.domain.GoalMilestone;
import com.locus.goal.domain.GoalStatus;
import com.locus.goal.domain.GoalType;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record GoalResponse(
    UUID id,
    GoalType goalType,
    String title,
    LocalDate targetDate,
    GoalStatus status,
    List<MilestoneResponse> milestones) {

  public static GoalResponse from(Goal goal, List<GoalMilestone> milestones) {
    return new GoalResponse(
        goal.getId(),
        goal.getGoalType(),
        goal.getTitle(),
        goal.getTargetDate(),
        goal.getStatus(),
        milestones.stream().map(MilestoneResponse::from).toList());
  }
}

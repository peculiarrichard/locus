package com.locus.goal.web.dto;

import com.locus.goal.domain.GoalMilestone;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record MilestoneResponse(
    UUID id, String milestoneName, String description, LocalDate dueDate, Instant completedAt) {

  public static MilestoneResponse from(GoalMilestone milestone) {
    return new MilestoneResponse(
        milestone.getId(),
        milestone.getMilestoneName(),
        milestone.getDescription(),
        milestone.getDueDate(),
        milestone.getCompletedAt());
  }
}

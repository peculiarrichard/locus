package com.locus.goal.service;

import com.locus.goal.domain.Goal;
import com.locus.goal.domain.GoalMilestone;
import com.locus.goal.domain.GoalSessionActivity;
import com.locus.goal.domain.GoalStatus;
import com.locus.goal.domain.PlanTemplate;
import com.locus.goal.exception.ApiException;
import com.locus.goal.repository.GoalMilestoneRepository;
import com.locus.goal.repository.GoalRepository;
import com.locus.goal.repository.GoalSessionActivityRepository;
import com.locus.goal.repository.PlanTemplateRepository;
import com.locus.goal.web.dto.CreateGoalRequest;
import com.locus.goal.web.dto.UpdateGoalRequest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Goal creation, milestone tracking, and lifecycle, per frd.md's Goal and Plan Service section.
@Service
public class GoalService {

  private final GoalRepository goalRepository;
  private final GoalMilestoneRepository goalMilestoneRepository;
  private final PlanTemplateRepository planTemplateRepository;
  private final GoalSessionActivityRepository goalSessionActivityRepository;

  public GoalService(
      GoalRepository goalRepository,
      GoalMilestoneRepository goalMilestoneRepository,
      PlanTemplateRepository planTemplateRepository,
      GoalSessionActivityRepository goalSessionActivityRepository) {
    this.goalRepository = goalRepository;
    this.goalMilestoneRepository = goalMilestoneRepository;
    this.planTemplateRepository = planTemplateRepository;
    this.goalSessionActivityRepository = goalSessionActivityRepository;
  }

  @Transactional
  public Goal create(UUID userId, CreateGoalRequest request) {
    if (!request.targetDate().isAfter(LocalDate.now(ZoneOffset.UTC))) {
      throw ApiException.badRequest("TARGET_DATE_IN_PAST", "targetDate must be in the future");
    }
    Goal goal = goalRepository.save(new Goal(userId, request.goalType(), request.title(), request.targetDate()));
    for (PlanTemplate template : planTemplateRepository.findByGoalType(request.goalType())) {
      goalMilestoneRepository.save(
          new GoalMilestone(
              goal.getId(),
              template.getMilestoneName(),
              template.getDescription(),
              template.getMilestoneOffsetDays(),
              request.targetDate().minusDays(template.getMilestoneOffsetDays())));
    }
    goalSessionActivityRepository.save(new GoalSessionActivity(goal.getId()));
    return goal;
  }

  @Transactional(readOnly = true)
  public Goal get(UUID userId, UUID goalId) {
    return getOwned(userId, goalId);
  }

  @Transactional(readOnly = true)
  public List<Goal> list(UUID userId) {
    return goalRepository.findByUserId(userId);
  }

  @Transactional(readOnly = true)
  public List<GoalMilestone> milestones(UUID goalId) {
    return goalMilestoneRepository.findByGoalId(goalId);
  }

  @Transactional
  public Goal update(UUID userId, UUID goalId, UpdateGoalRequest request) {
    Goal goal = getOwned(userId, goalId);
    if (goal.isTerminal()) {
      throw ApiException.conflict("GOAL_NOT_ACTIVE", "Only an active goal can be edited");
    }
    if (request.title() != null) {
      goal.setTitle(request.title());
    }
    if (request.targetDate() != null && !request.targetDate().equals(goal.getTargetDate())) {
      if (!request.targetDate().isAfter(LocalDate.now(ZoneOffset.UTC))) {
        throw ApiException.badRequest("TARGET_DATE_IN_PAST", "targetDate must be in the future");
      }
      goal.setTargetDate(request.targetDate());
      recomputeIncompleteMilestoneDueDates(goalId, request.targetDate());
    }
    return goalRepository.save(goal);
  }

  @Transactional
  public GoalMilestone completeMilestone(UUID userId, UUID goalId, UUID milestoneId) {
    getOwned(userId, goalId);
    GoalMilestone milestone = goalMilestoneRepository
        .findById(milestoneId)
        .filter(m -> m.getGoalId().equals(goalId))
        .orElseThrow(() -> ApiException.notFound("Milestone"));
    if (!milestone.isCompleted()) {
      milestone.setCompletedAt(Instant.now());
      goalMilestoneRepository.save(milestone);
    }
    return milestone;
  }

  @Transactional
  public Goal complete(UUID userId, UUID goalId) {
    return transitionFromActive(userId, goalId, GoalStatus.COMPLETED);
  }

  @Transactional
  public Goal abandon(UUID userId, UUID goalId) {
    return transitionFromActive(userId, goalId, GoalStatus.ABANDONED);
  }

  private Goal transitionFromActive(UUID userId, UUID goalId, GoalStatus target) {
    Goal goal = getOwned(userId, goalId);
    if (goal.isTerminal()) {
      throw ApiException.conflict("GOAL_NOT_ACTIVE", "Goal is already in a terminal state");
    }
    goal.setStatus(target);
    return goalRepository.save(goal);
  }

  private void recomputeIncompleteMilestoneDueDates(UUID goalId, LocalDate newTargetDate) {
    for (GoalMilestone milestone : goalMilestoneRepository.findByGoalIdAndCompletedAtIsNull(goalId)) {
      milestone.setDueDate(newTargetDate.minusDays(milestone.getMilestoneOffsetDays()));
      goalMilestoneRepository.save(milestone);
    }
  }

  private Goal getOwned(UUID userId, UUID goalId) {
    Goal goal = goalRepository.findById(goalId).orElseThrow(() -> ApiException.notFound("Goal"));
    if (!goal.getUserId().equals(userId)) {
      throw ApiException.forbidden("NOT_YOUR_GOAL", "Goal does not belong to this user");
    }
    return goal;
  }
}

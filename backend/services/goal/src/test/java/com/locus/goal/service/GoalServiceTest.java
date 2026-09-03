package com.locus.goal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.locus.goal.domain.Goal;
import com.locus.goal.domain.GoalMilestone;
import com.locus.goal.domain.GoalStatus;
import com.locus.goal.domain.GoalType;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.BeanUtils;
import org.springframework.test.util.ReflectionTestUtils;

class GoalServiceTest {

  private GoalRepository goalRepository;
  private GoalMilestoneRepository goalMilestoneRepository;
  private PlanTemplateRepository planTemplateRepository;
  private GoalSessionActivityRepository goalSessionActivityRepository;
  private GoalService goalService;
  private final UUID userId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    goalRepository = mock(GoalRepository.class);
    goalMilestoneRepository = mock(GoalMilestoneRepository.class);
    planTemplateRepository = mock(PlanTemplateRepository.class);
    goalSessionActivityRepository = mock(GoalSessionActivityRepository.class);
    goalService = new GoalService(goalRepository, goalMilestoneRepository, planTemplateRepository,
        goalSessionActivityRepository);
    when(goalRepository.save(any(Goal.class))).thenAnswer(inv -> inv.getArgument(0));
    when(goalMilestoneRepository.save(any(GoalMilestone.class))).thenAnswer(inv -> inv.getArgument(0));
  }

  @Test
  void createRejectsPastTargetDate() {
    CreateGoalRequest request = new CreateGoalRequest(GoalType.EXAM, "Finals", LocalDate.now().minusDays(1));
    assertThatThrownBy(() -> goalService.create(userId, request))
        .isInstanceOf(ApiException.class)
        .extracting("errorCode")
        .isEqualTo("TARGET_DATE_IN_PAST");
  }

  @Test
  void createCopiesTemplateMilestonesWithComputedDueDates() {
    LocalDate targetDate = LocalDate.now().plusDays(60);
    when(planTemplateRepository.findByGoalType(GoalType.EXAM))
        .thenReturn(
            List.of(template(GoalType.EXAM, 56, "Diagnostic review"), template(GoalType.EXAM, 7, "Final review")));

    goalService.create(userId, new CreateGoalRequest(GoalType.EXAM, "Finals", targetDate));

    ArgumentCaptor<GoalMilestone> captor = ArgumentCaptor.forClass(GoalMilestone.class);
    verify(goalMilestoneRepository, times(2)).save(captor.capture());
    assertThat(captor.getAllValues().get(0).getDueDate()).isEqualTo(targetDate.minusDays(56));
    assertThat(captor.getAllValues().get(1).getDueDate()).isEqualTo(targetDate.minusDays(7));
  }

  @Test
  void updatingTargetDateRecomputesOnlyIncompleteMilestones() {
    Goal goal = activeGoal(LocalDate.now().plusDays(30));
    when(goalRepository.findById(goal.getId())).thenReturn(Optional.of(goal));
    GoalMilestone incomplete = milestone(goal.getId(), 14);
    when(goalMilestoneRepository.findByGoalIdAndCompletedAtIsNull(goal.getId())).thenReturn(List.of(incomplete));

    LocalDate newTarget = LocalDate.now().plusDays(60);
    goalService.update(userId, goal.getId(), new UpdateGoalRequest(null, newTarget));

    assertThat(incomplete.getDueDate()).isEqualTo(newTarget.minusDays(14));
  }

  @Test
  void editingATerminalGoalIs409() {
    Goal goal = activeGoal(LocalDate.now().plusDays(30));
    goal.setStatus(GoalStatus.ABANDONED);
    when(goalRepository.findById(goal.getId())).thenReturn(Optional.of(goal));

    assertThatThrownBy(() -> goalService.update(userId, goal.getId(), new UpdateGoalRequest("New title", null)))
        .isInstanceOf(ApiException.class)
        .extracting("errorCode")
        .isEqualTo("GOAL_NOT_ACTIVE");
  }

  @Test
  void abandoningAnAlreadyAbandonedGoalIs409() {
    Goal goal = activeGoal(LocalDate.now().plusDays(30));
    goal.setStatus(GoalStatus.ABANDONED);
    when(goalRepository.findById(goal.getId())).thenReturn(Optional.of(goal));

    assertThatThrownBy(() -> goalService.abandon(userId, goal.getId()))
        .isInstanceOf(ApiException.class)
        .extracting("errorCode")
        .isEqualTo("GOAL_NOT_ACTIVE");
  }

  @Test
  void completingAMilestoneIsIdempotent() {
    Goal goal = activeGoal(LocalDate.now().plusDays(30));
    when(goalRepository.findById(goal.getId())).thenReturn(Optional.of(goal));
    GoalMilestone milestone = milestone(goal.getId(), 14);
    UUID milestoneId = UUID.randomUUID();
    ReflectionTestUtils.setField(milestone, "id", milestoneId);
    when(goalMilestoneRepository.findById(milestoneId)).thenReturn(Optional.of(milestone));
    when(goalMilestoneRepository.save(any(GoalMilestone.class))).thenAnswer(inv -> inv.getArgument(0));

    goalService.completeMilestone(userId, goal.getId(), milestoneId);
    assertThat(milestone.isCompleted()).isTrue();
    Instant firstCompletion = milestone.getCompletedAt();

    goalService.completeMilestone(userId, goal.getId(), milestoneId);
    assertThat(milestone.getCompletedAt()).isEqualTo(firstCompletion);
  }

  @Test
  void gettingAnotherUsersGoalIsForbidden() {
    Goal goal = activeGoal(LocalDate.now().plusDays(30));
    when(goalRepository.findById(goal.getId())).thenReturn(Optional.of(goal));

    assertThatThrownBy(() -> goalService.get(UUID.randomUUID(), goal.getId()))
        .isInstanceOf(ApiException.class)
        .extracting("errorCode")
        .isEqualTo("NOT_YOUR_GOAL");
  }

  private Goal activeGoal(LocalDate targetDate) {
    Goal goal = new Goal(userId, GoalType.EXAM, "Finals", targetDate);
    ReflectionTestUtils.setField(goal, "id", UUID.randomUUID());
    return goal;
  }

  private GoalMilestone milestone(UUID goalId, int offsetDays) {
    return new GoalMilestone(goalId, "Milestone", "desc", offsetDays, LocalDate.now().plusDays(offsetDays));
  }

  private PlanTemplate template(GoalType type, int offsetDays, String name) {
    PlanTemplate template = BeanUtils.instantiateClass(PlanTemplate.class);
    ReflectionTestUtils.setField(template, "goalType", type);
    ReflectionTestUtils.setField(template, "milestoneOffsetDays", offsetDays);
    ReflectionTestUtils.setField(template, "milestoneName", name);
    return template;
  }
}

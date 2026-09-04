package com.locus.goal.web;

import com.locus.goal.domain.Goal;
import com.locus.goal.service.GoalService;
import com.locus.goal.web.dto.CreateGoalRequest;
import com.locus.goal.web.dto.GoalResponse;
import com.locus.goal.web.dto.MilestoneResponse;
import com.locus.goal.web.dto.UpdateGoalRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// Goal, milestone, and lifecycle API surface, per frd.md's Goal and Plan Service section.
@RestController
public class GoalController {

  private final GoalService goalService;

  public GoalController(GoalService goalService) {
    this.goalService = goalService;
  }

  @PostMapping("/goals")
  @ResponseStatus(HttpStatus.CREATED)
  public GoalResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateGoalRequest request) {
    Goal goal = goalService.create(UUID.fromString(jwt.getSubject()), request);
    return toResponse(goal);
  }

  @GetMapping("/goals")
  public List<GoalResponse> list(@AuthenticationPrincipal Jwt jwt) {
    return goalService.list(UUID.fromString(jwt.getSubject())).stream().map(this::toResponse).toList();
  }

  @GetMapping("/goals/{id}")
  public GoalResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") UUID goalId) {
    return toResponse(goalService.get(UUID.fromString(jwt.getSubject()), goalId));
  }

  @PatchMapping("/goals/{id}")
  public GoalResponse update(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable("id") UUID goalId,
      @Valid @RequestBody UpdateGoalRequest request) {
    return toResponse(goalService.update(UUID.fromString(jwt.getSubject()), goalId, request));
  }

  @PostMapping("/goals/{id}/milestones/{milestoneId}/complete")
  public MilestoneResponse completeMilestone(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable("id") UUID goalId,
      @PathVariable("milestoneId") UUID milestoneId) {
    return MilestoneResponse.from(
        goalService.completeMilestone(UUID.fromString(jwt.getSubject()), goalId, milestoneId));
  }

  @PostMapping("/goals/{id}/complete")
  public GoalResponse complete(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") UUID goalId) {
    return toResponse(goalService.complete(UUID.fromString(jwt.getSubject()), goalId));
  }

  @PostMapping("/goals/{id}/abandon")
  public GoalResponse abandon(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") UUID goalId) {
    return toResponse(goalService.abandon(UUID.fromString(jwt.getSubject()), goalId));
  }

  private GoalResponse toResponse(Goal goal) {
    return GoalResponse.from(goal, goalService.milestones(goal.getId()));
  }
}

package com.locus.goal.service;

import com.locus.goal.domain.Goal;
import com.locus.goal.domain.GoalDeadlineNotification;
import com.locus.goal.domain.GoalStatus;
import com.locus.goal.event.EventPublisher;
import com.locus.goal.event.GoalDeadlineApproachingPayload;
import com.locus.goal.repository.GoalDeadlineNotificationRepository;
import com.locus.goal.repository.GoalRepository;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// Daily scan doing double duty: fires GoalDeadlineApproaching at the 30/14/7/1-day thresholds
// (frd.md), and flips a goal past its target_date with no user action to EXPIRED (frd.md's
// lifecycle section) — both are date-driven scans over this service's own active goals, so one
// job covers both rather than inventing a second identical scan.
@Component
public class GoalDeadlineScanJob {

  private static final List<Integer> THRESHOLDS = List.of(30, 14, 7, 1);

  private final GoalRepository goalRepository;
  private final GoalDeadlineNotificationRepository notificationRepository;
  private final EventPublisher eventPublisher;

  public GoalDeadlineScanJob(
      GoalRepository goalRepository,
      GoalDeadlineNotificationRepository notificationRepository,
      EventPublisher eventPublisher) {
    this.goalRepository = goalRepository;
    this.notificationRepository = notificationRepository;
    this.eventPublisher = eventPublisher;
  }

  @Scheduled(cron = "0 0 6 * * *")
  @Transactional
  public void scan() {
    LocalDate today = LocalDate.now(ZoneOffset.UTC);
    for (Goal goal : goalRepository.findByStatus(GoalStatus.ACTIVE)) {
      long daysRemaining = ChronoUnit.DAYS.between(today, goal.getTargetDate());
      if (daysRemaining < 0) {
        goal.setStatus(GoalStatus.EXPIRED);
        goalRepository.save(goal);
        continue;
      }
      fireDueThresholds(goal, daysRemaining);
    }
  }

  private void fireDueThresholds(Goal goal, long daysRemaining) {
    Set<Integer> alreadyFired = notificationRepository.findByGoalId(goal.getId()).stream()
        .map(GoalDeadlineNotification::getThresholdDays)
        .collect(Collectors.toSet());
    for (int threshold : THRESHOLDS) {
      if (daysRemaining <= threshold && !alreadyFired.contains(threshold)) {
        eventPublisher.publishGoalDeadlineApproaching(
            new GoalDeadlineApproachingPayload(
                goal.getUserId(), goal.getId(), goal.getGoalType(), goal.getTargetDate(), (int) daysRemaining));
        notificationRepository.save(new GoalDeadlineNotification(goal.getId(), threshold));
      }
    }
  }
}

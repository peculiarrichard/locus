package com.locus.goal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Read-only, static seed data (V1__init.sql) — never user-editable in v1, per frd.md.
@Entity
@Table(name = "plan_templates")
public class PlanTemplate {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "goal_type", nullable = false)
  private GoalType goalType;

  @Column(name = "milestone_offset_days", nullable = false)
  private int milestoneOffsetDays;

  @Column(name = "milestone_name", nullable = false)
  private String milestoneName;

  private String description;

  protected PlanTemplate() {
  }

  public GoalType getGoalType() {
    return goalType;
  }

  public int getMilestoneOffsetDays() {
    return milestoneOffsetDays;
  }

  public String getMilestoneName() {
    return milestoneName;
  }

  public String getDescription() {
    return description;
  }
}

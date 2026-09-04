package com.locus.goal.web.dto;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;

// Both fields optional — PATCH semantics, per frd.md's "title/target_date" API surface. @Size
// skips null (unset), so an omitted title still means "don't change" while a supplied one is
// bounded — a real gap found during Phase 12's security review: nothing previously stopped an
// arbitrarily long title from reaching the database.
public record UpdateGoalRequest(@Size(min = 1, max = 200) String title, LocalDate targetDate) {
}

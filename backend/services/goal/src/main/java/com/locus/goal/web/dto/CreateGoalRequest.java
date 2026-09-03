package com.locus.goal.web.dto;

import com.locus.goal.domain.GoalType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateGoalRequest(
    @NotNull GoalType goalType, @NotBlank @Size(max = 200) String title, @NotNull LocalDate targetDate) {
}

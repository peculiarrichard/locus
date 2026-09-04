package com.locus.accountability.web.dto;

import java.util.UUID;

// Deliberately minimal, per frd.md: only a binary "completed today" signal and a streak count —
// never session duration, distraction detail, or goal association.
public record MemberStatusResponse(UUID userId, boolean completedToday, int currentStreakDays) {
}

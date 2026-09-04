package com.locus.accountability.web.dto;

import com.locus.accountability.domain.GroupType;
import java.util.UUID;

// groupId: invite into an existing group the caller is already an active member of. Omitted:
// creates a new group of groupType (default PAIR) with the caller as its first member.
public record CreateInviteRequest(UUID groupId, GroupType groupType) {
}

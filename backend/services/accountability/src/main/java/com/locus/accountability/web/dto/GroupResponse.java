package com.locus.accountability.web.dto;

import com.locus.accountability.domain.AccountabilityGroup;
import com.locus.accountability.domain.GroupType;
import java.time.Instant;
import java.util.UUID;

public record GroupResponse(UUID id, GroupType groupType, UUID createdBy, Instant createdAt, int memberCount) {

  public static GroupResponse from(AccountabilityGroup group, int memberCount) {
    return new GroupResponse(group.getId(), group.getGroupType(), group.getCreatedBy(), group.getCreatedAt(),
        memberCount);
  }
}

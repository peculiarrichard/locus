package com.locus.accountability.web.dto;

import com.locus.accountability.domain.AccountabilityInvite;
import java.time.Instant;
import java.util.UUID;

public record InviteResponse(UUID id, String code, UUID groupId, Instant expiresAt) {

  public static InviteResponse from(AccountabilityInvite invite) {
    return new InviteResponse(invite.getId(), invite.getCode(), invite.getGroupId(), invite.getExpiresAt());
  }
}

package com.locus.accountability.web.dto;

import java.util.List;
import java.util.UUID;

public record GroupStatusResponse(UUID groupId, List<MemberStatusResponse> members) {
}

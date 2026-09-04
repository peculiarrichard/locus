package com.locus.accountability.repository;

import com.locus.accountability.domain.AccountabilityMember;
import com.locus.accountability.domain.MemberStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountabilityMemberRepository extends JpaRepository<AccountabilityMember, UUID> {

  List<AccountabilityMember> findByGroupIdAndStatus(UUID groupId, MemberStatus status);

  List<AccountabilityMember> findByUserIdAndStatus(UUID userId, MemberStatus status);

  Optional<AccountabilityMember> findByGroupIdAndUserId(UUID groupId, UUID userId);

  boolean existsByUserIdAndStatus(UUID userId, MemberStatus status);
}

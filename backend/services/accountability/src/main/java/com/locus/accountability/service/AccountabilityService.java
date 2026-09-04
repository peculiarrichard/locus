package com.locus.accountability.service;

import com.locus.accountability.domain.AccountabilityGroup;
import com.locus.accountability.domain.AccountabilityInvite;
import com.locus.accountability.domain.AccountabilityMember;
import com.locus.accountability.domain.GroupType;
import com.locus.accountability.domain.InviteStatus;
import com.locus.accountability.domain.MemberStatus;
import com.locus.accountability.event.AccountabilityPartnerActivityUpdatePayload;
import com.locus.accountability.event.EventPublisher;
import com.locus.accountability.exception.ApiException;
import com.locus.accountability.repository.AccountabilityGroupRepository;
import com.locus.accountability.repository.AccountabilityInviteRepository;
import com.locus.accountability.repository.AccountabilityMemberRepository;
import com.locus.accountability.repository.MemberActivityCompletionRepository;
import com.locus.accountability.web.dto.CreateInviteRequest;
import com.locus.accountability.web.dto.GroupResponse;
import com.locus.accountability.web.dto.GroupStatusResponse;
import com.locus.accountability.web.dto.MemberStatusResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Invite/pairing/group lifecycle, per frd.md's Accountability Service section.
@Service
public class AccountabilityService {

  private static final Duration INVITE_TTL = Duration.ofDays(7);

  private final AccountabilityGroupRepository groupRepository;
  private final AccountabilityMemberRepository memberRepository;
  private final AccountabilityInviteRepository inviteRepository;
  private final MemberActivityCompletionRepository completionRepository;
  private final InviteCodeGenerator inviteCodeGenerator;
  private final EventPublisher eventPublisher;

  public AccountabilityService(
      AccountabilityGroupRepository groupRepository,
      AccountabilityMemberRepository memberRepository,
      AccountabilityInviteRepository inviteRepository,
      MemberActivityCompletionRepository completionRepository,
      InviteCodeGenerator inviteCodeGenerator,
      EventPublisher eventPublisher) {
    this.groupRepository = groupRepository;
    this.memberRepository = memberRepository;
    this.inviteRepository = inviteRepository;
    this.completionRepository = completionRepository;
    this.inviteCodeGenerator = inviteCodeGenerator;
    this.eventPublisher = eventPublisher;
  }

  @Transactional
  public AccountabilityInvite createInvite(UUID userId, CreateInviteRequest request) {
    UUID groupId;
    if (request.groupId() != null) {
      AccountabilityGroup group = getGroup(request.groupId());
      requireActiveMember(group.getId(), userId);
      if (activeMemberCount(group.getId()) >= group.capacity()) {
        throw ApiException.conflict("GROUP_FULL", "Group is at capacity");
      }
      groupId = group.getId();
    } else {
      GroupType groupType = request.groupType() != null ? request.groupType() : GroupType.PAIR;
      AccountabilityGroup group = groupRepository.save(new AccountabilityGroup(groupType, userId));
      memberRepository.save(new AccountabilityMember(group.getId(), userId));
      groupId = group.getId();
    }
    String code = inviteCodeGenerator.generate();
    return inviteRepository.save(
        new AccountabilityInvite(code, groupId, userId, Instant.now().plus(INVITE_TTL)));
  }

  @Transactional
  public GroupResponse acceptInvite(UUID userId, String code) {
    AccountabilityInvite invite = inviteRepository.findByCode(code).orElseThrow(() -> ApiException.notFound("Invite"));
    if (!invite.isUsable()) {
      throw ApiException.badRequest("INVITE_NOT_USABLE", "Invite is expired or already used");
    }
    AccountabilityGroup group = getGroup(invite.getGroupId());
    if (memberRepository.findByGroupIdAndUserId(group.getId(), userId).filter(AccountabilityMember::isActive)
        .isPresent()) {
      throw ApiException.conflict("ALREADY_MEMBER", "Already an active member of this group");
    }
    if (activeMemberCount(group.getId()) >= group.capacity()) {
      throw ApiException.conflict("GROUP_FULL", "Group is at capacity");
    }

    List<AccountabilityMember> existingMembers = memberRepository.findByGroupIdAndStatus(group.getId(),
        MemberStatus.ACTIVE);
    memberRepository.save(new AccountabilityMember(group.getId(), userId));
    invite.setStatus(InviteStatus.ACCEPTED);
    inviteRepository.save(invite);

    for (AccountabilityMember existing : existingMembers) {
      eventPublisher.publishPartnerActivityUpdate(
          new AccountabilityPartnerActivityUpdatePayload(existing.getUserId(), userId, "member_joined", Instant.now()));
    }
    return GroupResponse.from(group, existingMembers.size() + 1);
  }

  @Transactional(readOnly = true)
  public List<GroupResponse> listGroups(UUID userId) {
    return memberRepository.findByUserIdAndStatus(userId, MemberStatus.ACTIVE).stream()
        .map(
            member -> {
              AccountabilityGroup group = getGroup(member.getGroupId());
              return GroupResponse.from(group, activeMemberCount(group.getId()));
            })
        .toList();
  }

  @Transactional(readOnly = true)
  public GroupStatusResponse groupStatus(UUID userId, UUID groupId) {
    requireActiveMember(groupId, userId);
    LocalDate today = LocalDate.now(ZoneOffset.UTC);
    List<MemberStatusResponse> members = memberRepository.findByGroupIdAndStatus(groupId, MemberStatus.ACTIVE).stream()
        .map(member -> new MemberStatusResponse(
            member.getUserId(),
            completionRepository.existsByUserIdAndCompletedDate(member.getUserId(), today),
            currentStreak(member.getUserId(), today)))
        .toList();
    return new GroupStatusResponse(groupId, members);
  }

  @Transactional
  public void leave(UUID userId, UUID groupId) {
    AccountabilityMember member = memberRepository
        .findByGroupIdAndUserId(groupId, userId)
        .filter(AccountabilityMember::isActive)
        .orElseThrow(() -> ApiException.notFound("Membership"));
    member.leave();
    memberRepository.save(member);
    if (activeMemberCount(groupId) == 0) {
      groupRepository.deleteById(groupId);
    }
  }

  @Transactional
  public void dissolve(UUID userId, UUID groupId) {
    AccountabilityGroup group = getGroup(groupId);
    if (!group.getCreatedBy().equals(userId)) {
      throw ApiException.forbidden("NOT_GROUP_OWNER", "Only the group owner can dissolve it");
    }
    groupRepository.deleteById(groupId);
  }

  // Per frd.md: a PAIR can't continue with one member, so it dissolves entirely on account
  // deletion; a larger GROUP just loses that one member (same as a normal leave), then applies
  // the usual last-member-leaving-auto-dissolves rule. Remaining members are notified either way
  // — frd.md's "remaining members see 'partner account deleted' rather than a silently broken
  // pairing," applied consistently to groups too, not just literal two-person pairs.
  @Transactional
  public void purgeUser(UUID userId) {
    for (AccountabilityMember membership : memberRepository.findByUserIdAndStatus(userId, MemberStatus.ACTIVE)) {
      AccountabilityGroup group = getGroup(membership.getGroupId());
      List<AccountabilityMember> others = memberRepository.findByGroupIdAndStatus(group.getId(), MemberStatus.ACTIVE)
          .stream()
          .filter(m -> !m.getUserId().equals(userId))
          .toList();
      for (AccountabilityMember other : others) {
        eventPublisher.publishPartnerActivityUpdate(
            new AccountabilityPartnerActivityUpdatePayload(other.getUserId(), userId, "account_deleted",
                Instant.now()));
      }
      if (group.getGroupType() == GroupType.PAIR) {
        groupRepository.deleteById(group.getId());
      } else {
        membership.leave();
        memberRepository.save(membership);
        if (others.isEmpty()) {
          groupRepository.deleteById(group.getId());
        }
      }
    }
    completionRepository.deleteByUserId(userId);
  }

  private int currentStreak(UUID userId, LocalDate today) {
    int streak = 0;
    LocalDate day = today;
    while (completionRepository.existsByUserIdAndCompletedDate(userId, day)) {
      streak++;
      day = day.minusDays(1);
    }
    return streak;
  }

  private int activeMemberCount(UUID groupId) {
    return memberRepository.findByGroupIdAndStatus(groupId, MemberStatus.ACTIVE).size();
  }

  private void requireActiveMember(UUID groupId, UUID userId) {
    memberRepository
        .findByGroupIdAndUserId(groupId, userId)
        .filter(AccountabilityMember::isActive)
        .orElseThrow(() -> ApiException.forbidden("NOT_A_MEMBER", "Not an active member of this group"));
  }

  private AccountabilityGroup getGroup(UUID groupId) {
    return groupRepository.findById(groupId).orElseThrow(() -> ApiException.notFound("Group"));
  }
}

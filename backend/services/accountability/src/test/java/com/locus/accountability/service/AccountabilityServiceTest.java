package com.locus.accountability.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.locus.accountability.domain.AccountabilityGroup;
import com.locus.accountability.domain.AccountabilityInvite;
import com.locus.accountability.domain.AccountabilityMember;
import com.locus.accountability.domain.GroupType;
import com.locus.accountability.domain.MemberStatus;
import com.locus.accountability.event.EventPublisher;
import com.locus.accountability.exception.ApiException;
import com.locus.accountability.repository.AccountabilityGroupRepository;
import com.locus.accountability.repository.AccountabilityInviteRepository;
import com.locus.accountability.repository.AccountabilityMemberRepository;
import com.locus.accountability.repository.MemberActivityCompletionRepository;
import com.locus.accountability.web.dto.CreateInviteRequest;
import com.locus.accountability.web.dto.GroupResponse;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class AccountabilityServiceTest {

  private AccountabilityGroupRepository groupRepository;
  private AccountabilityMemberRepository memberRepository;
  private AccountabilityInviteRepository inviteRepository;
  private MemberActivityCompletionRepository completionRepository;
  private EventPublisher eventPublisher;
  private AccountabilityService service;
  private final UUID userId = UUID.randomUUID();
  private final UUID otherUserId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    groupRepository = mock(AccountabilityGroupRepository.class);
    memberRepository = mock(AccountabilityMemberRepository.class);
    inviteRepository = mock(AccountabilityInviteRepository.class);
    completionRepository = mock(MemberActivityCompletionRepository.class);
    eventPublisher = mock(EventPublisher.class);
    service = new AccountabilityService(
        groupRepository, memberRepository, inviteRepository, completionRepository, new InviteCodeGenerator(),
        eventPublisher);
    when(groupRepository.save(any(AccountabilityGroup.class))).thenAnswer(inv -> inv.getArgument(0));
    when(memberRepository.save(any(AccountabilityMember.class))).thenAnswer(inv -> inv.getArgument(0));
    when(inviteRepository.save(any(AccountabilityInvite.class))).thenAnswer(inv -> inv.getArgument(0));
  }

  @Test
  void creatingAnInviteWithNoGroupIdCreatesAPairByDefault() {
    AccountabilityInvite invite = service.createInvite(userId, new CreateInviteRequest(null, null));
    assertThat(invite.getCode()).hasSize(8);
    assertThat(invite.getExpiresAt()).isAfter(Instant.now());
  }

  @Test
  void acceptingAFullGroupInviteIs409() {
    AccountabilityGroup pair = pairGroup();
    AccountabilityInvite invite = invite(pair.getId());
    when(inviteRepository.findByCode(invite.getCode())).thenReturn(Optional.of(invite));
    when(groupRepository.findById(pair.getId())).thenReturn(Optional.of(pair));
    when(memberRepository.findByGroupIdAndUserId(pair.getId(), otherUserId)).thenReturn(Optional.empty());
    when(memberRepository.findByGroupIdAndStatus(pair.getId(), MemberStatus.ACTIVE))
        .thenReturn(List.of(new AccountabilityMember(pair.getId(), userId),
            new AccountabilityMember(pair.getId(), UUID.randomUUID())));

    assertThatThrownBy(() -> service.acceptInvite(otherUserId, invite.getCode()))
        .isInstanceOf(ApiException.class)
        .extracting("errorCode")
        .isEqualTo("GROUP_FULL");
  }

  @Test
  void acceptingAnExpiredInviteFails() {
    AccountabilityGroup pair = pairGroup();
    AccountabilityInvite invite = invite(pair.getId());
    ReflectionTestUtils.setField(invite, "expiresAt", Instant.now().minusSeconds(10));
    when(inviteRepository.findByCode(invite.getCode())).thenReturn(Optional.of(invite));

    assertThatThrownBy(() -> service.acceptInvite(otherUserId, invite.getCode()))
        .isInstanceOf(ApiException.class)
        .extracting("errorCode")
        .isEqualTo("INVITE_NOT_USABLE");
  }

  @Test
  void acceptingAnInviteNotifiesExistingActiveMembers() {
    AccountabilityGroup pair = pairGroup();
    AccountabilityInvite invite = invite(pair.getId());
    when(inviteRepository.findByCode(invite.getCode())).thenReturn(Optional.of(invite));
    when(groupRepository.findById(pair.getId())).thenReturn(Optional.of(pair));
    when(memberRepository.findByGroupIdAndUserId(pair.getId(), otherUserId)).thenReturn(Optional.empty());
    when(memberRepository.findByGroupIdAndStatus(pair.getId(), MemberStatus.ACTIVE))
        .thenReturn(List.of(new AccountabilityMember(pair.getId(), userId)));

    GroupResponse result = service.acceptInvite(otherUserId, invite.getCode());

    assertThat(result.memberCount()).isEqualTo(2);
    org.mockito.Mockito.verify(eventPublisher).publishPartnerActivityUpdate(
        org.mockito.ArgumentMatchers
            .argThat(p -> p.userId().equals(userId) && p.activityType().equals("member_joined")));
  }

  @Test
  void gettingStatusForANonMemberIsForbidden() {
    AccountabilityGroup pair = pairGroup();
    when(memberRepository.findByGroupIdAndUserId(pair.getId(), userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.groupStatus(userId, pair.getId()))
        .isInstanceOf(ApiException.class)
        .extracting("errorCode")
        .isEqualTo("NOT_A_MEMBER");
  }

  @Test
  void leavingAsTheLastActiveMemberDissolvesTheGroup() {
    AccountabilityGroup pair = pairGroup();
    AccountabilityMember membership = new AccountabilityMember(pair.getId(), userId);
    when(memberRepository.findByGroupIdAndUserId(pair.getId(), userId)).thenReturn(Optional.of(membership));
    when(memberRepository.findByGroupIdAndStatus(pair.getId(), MemberStatus.ACTIVE)).thenReturn(List.of());

    service.leave(userId, pair.getId());

    org.mockito.Mockito.verify(groupRepository).deleteById(pair.getId());
  }

  @Test
  void dissolvingAsNonOwnerIsForbidden() {
    AccountabilityGroup pair = pairGroup();
    when(groupRepository.findById(pair.getId())).thenReturn(Optional.of(pair));

    assertThatThrownBy(() -> service.dissolve(otherUserId, pair.getId()))
        .isInstanceOf(ApiException.class)
        .extracting("errorCode")
        .isEqualTo("NOT_GROUP_OWNER");
  }

  private AccountabilityGroup pairGroup() {
    AccountabilityGroup group = new AccountabilityGroup(GroupType.PAIR, userId);
    ReflectionTestUtils.setField(group, "id", UUID.randomUUID());
    return group;
  }

  private AccountabilityInvite invite(UUID groupId) {
    return new AccountabilityInvite("ABCD1234", groupId, userId, Instant.now().plusSeconds(3600));
  }
}

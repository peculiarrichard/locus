package com.locus.accountability.web;

import com.locus.accountability.service.AccountabilityService;
import com.locus.accountability.web.dto.CreateInviteRequest;
import com.locus.accountability.web.dto.GroupResponse;
import com.locus.accountability.web.dto.GroupStatusResponse;
import com.locus.accountability.web.dto.InviteResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// Invite/pairing/group API surface, per frd.md's Accountability Service section.
@RestController
public class AccountabilityController {

  private final AccountabilityService accountabilityService;

  public AccountabilityController(AccountabilityService accountabilityService) {
    this.accountabilityService = accountabilityService;
  }

  @PostMapping("/accountability/invites")
  @ResponseStatus(HttpStatus.CREATED)
  public InviteResponse createInvite(@AuthenticationPrincipal Jwt jwt, @RequestBody CreateInviteRequest request) {
    return InviteResponse.from(accountabilityService.createInvite(UUID.fromString(jwt.getSubject()), request));
  }

  @PostMapping("/accountability/invites/{code}/accept")
  public GroupResponse acceptInvite(@AuthenticationPrincipal Jwt jwt, @PathVariable String code) {
    return accountabilityService.acceptInvite(UUID.fromString(jwt.getSubject()), code);
  }

  @GetMapping("/accountability/groups")
  public List<GroupResponse> listGroups(@AuthenticationPrincipal Jwt jwt) {
    return accountabilityService.listGroups(UUID.fromString(jwt.getSubject()));
  }

  @GetMapping("/accountability/groups/{id}/status")
  public GroupStatusResponse groupStatus(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") UUID groupId) {
    return accountabilityService.groupStatus(UUID.fromString(jwt.getSubject()), groupId);
  }

  @PostMapping("/accountability/groups/{id}/leave")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void leave(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") UUID groupId) {
    accountabilityService.leave(UUID.fromString(jwt.getSubject()), groupId);
  }

  @DeleteMapping("/accountability/groups/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void dissolve(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") UUID groupId) {
    accountabilityService.dissolve(UUID.fromString(jwt.getSubject()), groupId);
  }
}

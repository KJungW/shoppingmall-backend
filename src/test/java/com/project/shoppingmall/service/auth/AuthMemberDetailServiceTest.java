package com.project.shoppingmall.service.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.dto.auth.AuthMemberDetail;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.service.member.MemberFindService;
import com.project.shoppingmall.test_entity.member.MemberBuilder;
import com.project.shoppingmall.type.MemberRoleType;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AuthMemberDetailServiceTest {
  private AuthMemberDetailService target;
  private MemberFindService mockMemberFindService;

  @BeforeEach
  public void beforeEach() {
    mockMemberFindService = mock(MemberFindService.class);
    target = new AuthMemberDetailService(mockMemberFindService);
  }

  @Test
  @DisplayName("loadUserByUsername() : 정상흐름")
  public void loadUserByUsername_ok() {
    // given
    Long inputMemberId = 10L;
    String inputMemberIdStr = String.valueOf(inputMemberId);

    Member givenMember = MemberBuilder.makeMember(inputMemberId, MemberRoleType.ROLE_MEMBER);
    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.of(givenMember));

    // when
    AuthMemberDetail resultUserDetail =
        (AuthMemberDetail) target.loadUserByUsername(inputMemberIdStr);

    // then
    checkAuthMemberDetail(resultUserDetail, givenMember);
  }

  @Test
  @DisplayName("loadUserByUsername() : 조회된 회원이 없음")
  public void loadUserByUsername_NoMember() {
    // given
    Long givenMemberId = 10L;
    String rightMemberId = String.valueOf(givenMemberId);

    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.empty());

    // when
    assertThrows(DataNotFound.class, () -> target.loadUserByUsername(rightMemberId));
  }

  public void checkAuthMemberDetail(AuthMemberDetail target, Member givenMember) {
    assertEquals(givenMember.getId(), target.getId());
    assertEquals(givenMember.getRole(), target.getRole());
    assertEquals(givenMember.getId().toString(), target.getUsername());
    assertNull(target.getPassword());
    assertEquals(1, target.getAuthorities().size());
    assertEquals(
        givenMember.getRole().toString(), target.getAuthorities().iterator().next().getAuthority());
  }
}

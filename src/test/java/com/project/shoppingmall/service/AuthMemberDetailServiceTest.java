package com.project.shoppingmall.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.dto.auth.AuthUserDetail;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.service.auth.AuthMemberDetailService;
import com.project.shoppingmall.service.member.MemberService;
import com.project.shoppingmall.testdata.MemberBuilder;
import com.project.shoppingmall.type.MemberRoleType;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class AuthMemberDetailServiceTest {
  private AuthMemberDetailService target;
  private MemberService mockMemberService;

  @BeforeEach
  public void beforeEach() {
    mockMemberService = mock(MemberService.class);
    target = new AuthMemberDetailService(mockMemberService);
  }

  @Test
  @DisplayName("loadUserByUsername() : 정상흐름")
  public void loadUserByUsername_ok() {
    // given
    // - username 인자 세팅
    Long givenMemberId = 10L;
    String rightMemberId = String.valueOf(givenMemberId);

    // - memberService.findById() 세팅
    MemberRoleType givenRoleType = MemberRoleType.ROLE_MEMBER;
    Member givenMember = MemberBuilder.fullData().role(givenRoleType).build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);
    when(mockMemberService.findById(anyLong())).thenReturn(Optional.of(givenMember));

    // when
    AuthUserDetail resultUserDetail = (AuthUserDetail) target.loadUserByUsername(rightMemberId);

    // then
    assertEquals(givenMemberId, resultUserDetail.getId());
    assertEquals(givenRoleType, resultUserDetail.getRole());
    assertEquals(rightMemberId, resultUserDetail.getUsername());
    assertNull(resultUserDetail.getPassword());
    assertEquals(1, resultUserDetail.getAuthorities().size());
    assertEquals(
        MemberRoleType.ROLE_MEMBER.toString(),
        resultUserDetail.getAuthorities().iterator().next().getAuthority());
  }

  @Test
  @DisplayName("loadUserByUsername() : 조회된 회원이 없음")
  public void loadUserByUsername_NoMember() {
    // given
    // - username 인자 세팅
    Long givenMemberId = 10L;
    String rightMemberId = String.valueOf(givenMemberId);

    // - memberService.findById() 세팅
    when(mockMemberService.findById(anyLong())).thenReturn(Optional.empty());

    // when
    assertThrows(DataNotFound.class, () -> target.loadUserByUsername(rightMemberId));
  }
}

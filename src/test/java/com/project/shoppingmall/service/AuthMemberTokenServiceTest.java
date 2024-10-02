package com.project.shoppingmall.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.dto.token.RefreshAndAccessToken;
import com.project.shoppingmall.dto.token.RefreshTokenData;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.MemberToken;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.exception.JwtTokenException;
import com.project.shoppingmall.service.auth.AuthMemberTokenService;
import com.project.shoppingmall.service.member.MemberFindService;
import com.project.shoppingmall.testdata.member.MemberBuilder;
import com.project.shoppingmall.type.MemberRoleType;
import com.project.shoppingmall.util.JwtUtil;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class AuthMemberTokenServiceTest {
  private AuthMemberTokenService target;
  private MemberFindService mockMemberFindService;
  private JwtUtil mockJwtUtil;

  @BeforeEach
  public void beforeEach() {
    mockMemberFindService = mock(MemberFindService.class);
    mockJwtUtil = mock(JwtUtil.class);
    target = new AuthMemberTokenService(mockMemberFindService, mockJwtUtil);
  }

  @Test
  @DisplayName("reissueRefreshAndAccess() : 정상흐름")
  public void reissueRefreshAndAccess_ok() {
    // given
    // - inputRefreshToken 인자 세팅
    String rightInputRefreshToken = "testInputRefreshToken";

    // - jwtUtil.decodeRefreshToken() 세팅
    Long givenMemberId = 10L;
    RefreshTokenData givenInputRefreshData =
        new RefreshTokenData(givenMemberId, MemberRoleType.ROLE_MEMBER.toString());
    when(mockJwtUtil.decodeRefreshToken(anyString())).thenReturn(givenInputRefreshData);

    // - memberService.findById() 세팅
    Member givenMember =
        MemberBuilder.fullData().token(new MemberToken(rightInputRefreshToken)).build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);
    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.of(givenMember));

    // - jwtUtil.createAccessToken() 세팅
    String givenReissueAccess = "testReissueAccessToken";
    when(mockJwtUtil.createAccessToken(any())).thenReturn(givenReissueAccess);

    // - jwtUtil.createRefreshToken() 세팅
    String givenReissueRefresh = "testReissueRefreshToken";
    when(mockJwtUtil.createRefreshToken(any())).thenReturn(givenReissueRefresh);

    // when
    RefreshAndAccessToken result = target.reissueRefreshAndAccess(rightInputRefreshToken);

    // then
    // - 재발급된 refreshToken 검증
    assertEquals(givenReissueRefresh, result.getRefreshToken());

    // - 재발급된 accessToken 검증
    assertEquals(givenReissueAccess, result.getAccessToken());

    // - Member.refresh 필드가 제대로 업데이트 되었는지 확인
    assertEquals(givenReissueRefresh, givenMember.getToken().getRefresh());
  }

  @Test
  @DisplayName("reissueRefreshAndAccess() : DB에 저장되어있지 않은 리프레쉬 토큰 입력")
  public void reissueRefreshAndAccess_WrongRefreshToken() {
    // given
    // - inputRefreshToken 인자 세팅
    String wrongInputRefreshToken = "wrongInputRefreshToken";

    // - jwtUtil.decodeRefreshToken() 세팅
    Long givenMemberId = 10L;
    RefreshTokenData givenInputRefreshData =
        new RefreshTokenData(givenMemberId, MemberRoleType.ROLE_MEMBER.toString());
    when(mockJwtUtil.decodeRefreshToken(anyString())).thenReturn(givenInputRefreshData);

    // - memberService.findById() 세팅
    String givenRefreshTokenInDb = "testInputRefreshTokenInDB";
    Member givenMember =
        MemberBuilder.fullData().token(new MemberToken(givenRefreshTokenInDb)).build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);
    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.of(givenMember));

    // when then
    assertThrows(
        JwtTokenException.class, () -> target.reissueRefreshAndAccess(wrongInputRefreshToken));
  }

  @Test
  @DisplayName("deleteRefreshToken() : 정상흐름")
  public void deleteRefreshToken_ok() {
    // given
    // - memberId 인자 세팅
    Long rightMemberId = 10L;

    // - mockMemberService.findById() 세팅
    String givenRefreshTokenInDb = "testInputRefreshTokenInDB";
    Member givenMember =
        MemberBuilder.fullData().token(new MemberToken(givenRefreshTokenInDb)).build();
    ReflectionTestUtils.setField(givenMember, "id", rightMemberId);
    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.of(givenMember));

    // when
    target.deleteRefreshToken(rightMemberId);

    // then
    assertNull(givenMember.getToken());
  }

  @Test
  @DisplayName("deleteRefreshToken() : 존재하지 않는 회원이 토큰 제거를 시도")
  public void deleteRefreshToken_noMember() {
    // given
    // - memberId 인자 세팅
    Long rightMemberId = 10L;

    // - mockMemberService.findById() 세팅
    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.empty());

    // when
    assertThrows(DataNotFound.class, () -> target.deleteRefreshToken(rightMemberId));
  }
}

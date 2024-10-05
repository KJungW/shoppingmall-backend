package com.project.shoppingmall.service.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.dto.token.RefreshAndAccessToken;
import com.project.shoppingmall.dto.token.RefreshTokenData;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.exception.JwtTokenException;
import com.project.shoppingmall.service.member.MemberFindService;
import com.project.shoppingmall.test_entity.member.MemberBuilder;
import com.project.shoppingmall.type.MemberRoleType;
import com.project.shoppingmall.util.JwtUtil;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
    String inputRefreshToken = "testInputRefreshToken";
    Long memberIdInInputToken = 10L;
    MemberRoleType memberRoleInInputToken = MemberRoleType.ROLE_MEMBER;

    RefreshTokenData givenRefreshTokenDecodeResult =
        new RefreshTokenData(memberIdInInputToken, memberRoleInInputToken.toString());
    Member givenMember =
        MemberBuilder.makeMember(memberIdInInputToken, memberRoleInInputToken, inputRefreshToken);
    String givenReissueAccess = "testReissueAccessToken";
    String givenReissueRefresh = "testReissueRefreshToken";

    when(mockJwtUtil.decodeRefreshToken(anyString())).thenReturn(givenRefreshTokenDecodeResult);
    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.of(givenMember));
    when(mockJwtUtil.createAccessToken(any())).thenReturn(givenReissueAccess);
    when(mockJwtUtil.createRefreshToken(any())).thenReturn(givenReissueRefresh);

    // when
    RefreshAndAccessToken result = target.reissueRefreshAndAccess(inputRefreshToken);

    // then
    assertEquals(givenReissueRefresh, result.getRefreshToken());
    assertEquals(givenReissueAccess, result.getAccessToken());
    assertEquals(givenReissueRefresh, givenMember.getToken().getRefresh());
  }

  @Test
  @DisplayName("reissueRefreshAndAccess() : DB에 저장되어있지 않은 리프레쉬 토큰 입력")
  public void reissueRefreshAndAccess_WrongRefreshToken() {
    // given
    String inputOldRefreshToken = "inputOldRefreshToken";
    Long memberIdInInputToken = 10L;
    MemberRoleType memberRoleInInputToken = MemberRoleType.ROLE_MEMBER;

    RefreshTokenData givenOldRefreshTokenDecodeResult =
        new RefreshTokenData(memberIdInInputToken, memberRoleInInputToken.toString());
    Member givenMember =
        MemberBuilder.makeMember(
            memberIdInInputToken, memberRoleInInputToken, "LatestRefreshToken");
    String givenReissueAccess = "testReissueAccessToken";
    String givenReissueRefresh = "testReissueRefreshToken";

    when(mockJwtUtil.decodeRefreshToken(anyString())).thenReturn(givenOldRefreshTokenDecodeResult);
    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.of(givenMember));
    when(mockJwtUtil.createAccessToken(any())).thenReturn(givenReissueAccess);
    when(mockJwtUtil.createRefreshToken(any())).thenReturn(givenReissueRefresh);

    // when then
    assertThrows(
        JwtTokenException.class, () -> target.reissueRefreshAndAccess(inputOldRefreshToken));
  }

  @Test
  @DisplayName("deleteRefreshToken() : 정상흐름")
  public void deleteRefreshToken_ok() {
    // given
    Long inputMemberId = 10L;

    Member givenMember =
        MemberBuilder.makeMember(inputMemberId, MemberRoleType.ROLE_MEMBER, "refreshToken");
    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.of(givenMember));

    // when
    target.deleteRefreshToken(inputMemberId);

    // then
    assertNull(givenMember.getToken());
  }

  @Test
  @DisplayName("deleteRefreshToken() : 존재하지 않는 회원이 토큰 제거를 시도")
  public void deleteRefreshToken_noMember() {
    // given
    Long inputMemberId = 10L;

    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.empty());

    // when
    assertThrows(DataNotFound.class, () -> target.deleteRefreshToken(inputMemberId));
  }
}

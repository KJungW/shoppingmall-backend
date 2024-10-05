package com.project.shoppingmall.handler.oauth2;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.dto.oauth2.OAuth2UserPrinciple;
import com.project.shoppingmall.dto.oauth2.user_info.OAuth2UserInfo;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.service.member.MemberFindService;
import com.project.shoppingmall.service.member.MemberService;
import com.project.shoppingmall.test_entity.member.MemberBuilder;
import com.project.shoppingmall.type.LoginType;
import com.project.shoppingmall.type.MemberRoleType;
import com.project.shoppingmall.util.CookieUtil;
import com.project.shoppingmall.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

class OAuth2SuccessHandlerTest {

  private OAuth2SuccessHandler target;
  private MemberFindService mockMemberFindService;
  private MemberService mockMemberService;
  private JwtUtil mockJwtUtil;
  private CookieUtil mockCookieUtil;
  private final String givenRedirectionUrl = "/test/test/login/success";

  @BeforeEach
  public void beforeEach() {
    mockMemberFindService = mock(MemberFindService.class);
    mockMemberService = mock(MemberService.class);
    mockJwtUtil = mock(JwtUtil.class);
    mockCookieUtil = mock(CookieUtil.class);
    target =
        new OAuth2SuccessHandler(
            mockMemberFindService, mockMemberService, mockJwtUtil, mockCookieUtil);

    ReflectionTestUtils.setField(target, "loginSuccessRedirectionUrl", givenRedirectionUrl);
  }

  @Test
  @DisplayName("OAuth2SuccessHandler.onAuthenticationSuccess() : 정상흐름(이미 멤버가 존재할때)")
  void onAuthenticationSuccess_ok_memberExist() throws ServletException, IOException {
    // given
    HttpServletRequest inputRequest = new MockHttpServletRequest();
    HttpServletResponse inputResponse = new MockHttpServletResponse();
    Authentication inputAuthentication =
        setMockAuthentication("testName1234", LoginType.NAVER, "d123sfdsfds");

    Member givenMember = MemberBuilder.makeMember(3210L);
    String givenRefreshToken = "asdkjlajdklqwle123123xcvczxvcxv";
    String givenCookieKey = "refresh";

    when(mockMemberFindService.findByLonginTypeAndSocialId(any(), any()))
        .thenReturn(Optional.of(givenMember));
    ;
    when(mockJwtUtil.createRefreshToken(any())).thenReturn(givenRefreshToken);
    when(mockCookieUtil.createCookie(any(), any(), anyInt()))
        .thenReturn(ResponseCookie.from(givenCookieKey, givenRefreshToken).build());

    // when
    target.onAuthenticationSuccess(inputRequest, inputResponse, inputAuthentication);

    // then
    checkRefreshTokenUpdateInMember(givenRefreshToken, givenMember);
    checkNotCreateMember();
    checkResponseResult(givenCookieKey, givenRefreshToken, inputResponse);
  }

  @Test
  @DisplayName("OAuth2SuccessHandler.onAuthenticationSuccess() : 정상흐름(멤버가 존재하지 않을때)")
  void onAuthenticationSuccess_ok_memberNotExist() throws ServletException, IOException {
    // given
    String givenUserName = "testName1234";
    LoginType givenLoginType = LoginType.NAVER;
    String givenSocialId = "d123sfdsfds";

    HttpServletRequest inputRequest = new MockHttpServletRequest();
    HttpServletResponse inputResponse = new MockHttpServletResponse();
    Authentication inputAuthentication =
        setMockAuthentication(givenUserName, givenLoginType, givenSocialId);

    String givenRefreshToken = "asdkjlajdklqwle123123xcvczxvcxv";
    String givenCookieKey = "refresh";

    when(mockMemberFindService.findByLonginTypeAndSocialId(any(), any()))
        .thenReturn(Optional.empty());
    ;
    when(mockJwtUtil.createRefreshToken(any())).thenReturn(givenRefreshToken);
    when(mockCookieUtil.createCookie(any(), any(), anyInt()))
        .thenReturn(ResponseCookie.from(givenCookieKey, givenRefreshToken).build());

    // when
    target.onAuthenticationSuccess(inputRequest, inputResponse, inputAuthentication);

    // then
    checkCreateMember(givenUserName, givenLoginType, givenSocialId);
    checkResponseResult(givenCookieKey, givenRefreshToken, inputResponse);
  }

  public Authentication setMockAuthentication(
      String userName, LoginType loginType, String socialId) {
    OAuth2UserInfo mockUserInfo = mock(OAuth2UserInfo.class);
    when(mockUserInfo.getName()).thenReturn(userName);
    when(mockUserInfo.getLoginType()).thenReturn(loginType);
    when(mockUserInfo.getSocialId()).thenReturn(socialId);

    OAuth2UserPrinciple mockUserPrinciple = mock(OAuth2UserPrinciple.class);
    when(mockUserPrinciple.getUserInfo()).thenReturn(mockUserInfo);

    Authentication authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(mockUserPrinciple);
    return authentication;
  }

  public void checkResponseResult(
      String givenCookieKey, String givenRefreshToken, HttpServletResponse response) {
    MockHttpServletResponse resultResponse = (MockHttpServletResponse) response;
    assertEquals(302, resultResponse.getStatus());
    assertEquals(givenRedirectionUrl, resultResponse.getRedirectedUrl());
    assertEquals(givenRefreshToken, resultResponse.getCookie(givenCookieKey).getValue());
  }

  public void checkRefreshTokenUpdateInMember(String refreshToken, Member member) {
    assertEquals(refreshToken, member.getToken().getRefresh());
  }

  public void checkNotCreateMember() {
    verify(mockMemberService, times(0)).save(any());
  }

  public void checkCreateMember(String userName, LoginType loginType, String socialId) {
    ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
    verify(mockMemberService, times(1)).save(memberCaptor.capture());

    Member memberArg = memberCaptor.getValue();
    assertEquals(loginType, memberArg.getLoginType());
    assertEquals(socialId, memberArg.getSocialId());
    assertEquals(userName, memberArg.getNickName());
    assertEquals(MemberRoleType.ROLE_MEMBER, memberArg.getRole());
    assertEquals(false, memberArg.getIsBan());
  }
}

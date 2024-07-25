package com.project.shoppingmall.handler.oauth2;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.dto.oauth2.OAuth2UserPrinciple;
import com.project.shoppingmall.dto.oauth2.user_info.OAuth2UserInfo;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.service.MemberService;
import com.project.shoppingmall.testdata.MemberBuilder;
import com.project.shoppingmall.util.CookieUtil;
import com.project.shoppingmall.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

class OAuth2SuccessHandlerTest {

  private OAuth2SuccessHandler target;
  private MemberService mockMemberService;
  private JwtUtil mockJwtUtil;
  private CookieUtil mockCookieUtil;
  private HttpServletRequest mockRequest;
  private HttpServletResponse mockResponse;
  private Authentication mockAuthentication;

  @BeforeEach
  public void beforeEach() {
    // target 구성
    mockMemberService = mock(MemberService.class);
    mockJwtUtil = mock(JwtUtil.class);
    mockCookieUtil = mock(CookieUtil.class);
    target = new OAuth2SuccessHandler(mockMemberService, mockJwtUtil, mockCookieUtil);

    // 테스트할 메서드 인수 구성
    mockRequest = new MockHttpServletRequest();
    mockResponse = new MockHttpServletResponse();
    mockAuthentication = mock(Authentication.class);
  }

  @Test
  @DisplayName("OAuth2SuccessHandler.onAuthenticationSuccess() : 정상흐름(이미 멤버가 존재할때)")
  void onAuthenticationSuccess_ok_memberExist() throws ServletException, IOException {
    // given
    String givenRedirectionUrl = "/test/test/login/success";
    ReflectionTestUtils.setField(target, "loginSuccessRedirectionUrl", givenRedirectionUrl);

    OAuth2UserPrinciple mockUserPrinciple = mock(OAuth2UserPrinciple.class);
    when(mockAuthentication.getPrincipal()).thenReturn(mockUserPrinciple);
    OAuth2UserInfo mockUserInfo = mock(OAuth2UserInfo.class);
    when(mockUserPrinciple.getUserInfo()).thenReturn(mockUserInfo);
    String givenMemberName = "testName1234";
    when(mockUserInfo.getName()).thenReturn(givenMemberName);

    Member givenMember = MemberBuilder.fullData().build();
    when(mockMemberService.findByLonginTypeAndSocialId(any(), any()))
        .thenReturn(Optional.of(givenMember));

    String givenRefreshToken = "asdkjlajdklqwle123123xcvczxvcxv";
    when(mockJwtUtil.createAccessToken(any())).thenReturn(givenRefreshToken);

    String givenCookieKey = "refresh";
    when(mockCookieUtil.createCookie(any(), any(), anyInt()))
        .thenReturn(new Cookie(givenCookieKey, givenRefreshToken));

    // when
    target.onAuthenticationSuccess(mockRequest, mockResponse, mockAuthentication);

    // then
    MockHttpServletResponse resultResponse = (MockHttpServletResponse) mockResponse;
    assertEquals(302, resultResponse.getStatus());
    assertEquals(givenRedirectionUrl, resultResponse.getRedirectedUrl());
    assertEquals(givenRefreshToken, resultResponse.getCookie(givenCookieKey).getValue());
    verify(mockMemberService, times(1)).findByLonginTypeAndSocialId(any(), any());
  }

  @Test
  @DisplayName("OAuth2SuccessHandler.onAuthenticationSuccess() : 정상흐름(멤버가 존재하지 않을때)")
  void onAuthenticationSuccess_ok_memberNotExist() throws ServletException, IOException {
    // given
    String givenRedirectionUrl = "/test/test/login/success";
    ReflectionTestUtils.setField(target, "loginSuccessRedirectionUrl", givenRedirectionUrl);

    OAuth2UserPrinciple mockUserPrinciple = mock(OAuth2UserPrinciple.class);
    when(mockAuthentication.getPrincipal()).thenReturn(mockUserPrinciple);
    OAuth2UserInfo mockUserInfo = mock(OAuth2UserInfo.class);
    when(mockUserPrinciple.getUserInfo()).thenReturn(mockUserInfo);
    String givenMemberName = "testName1234";
    when(mockUserInfo.getName()).thenReturn(givenMemberName);

    when(mockMemberService.findByLonginTypeAndSocialId(any(), any())).thenReturn(Optional.empty());

    String givenRefreshToken = "asdkjlajdklqwle123123xcvczxvcxv";
    when(mockJwtUtil.createAccessToken(any())).thenReturn(givenRefreshToken);

    String givenCookieKey = "refresh";
    when(mockCookieUtil.createCookie(any(), any(), anyInt()))
        .thenReturn(new Cookie(givenCookieKey, givenRefreshToken));

    // when
    target.onAuthenticationSuccess(mockRequest, mockResponse, mockAuthentication);

    // then
    MockHttpServletResponse resultResponse = (MockHttpServletResponse) mockResponse;
    assertEquals(302, resultResponse.getStatus());
    assertEquals(givenRedirectionUrl, resultResponse.getRedirectedUrl());
    assertEquals(givenRefreshToken, resultResponse.getCookie(givenCookieKey).getValue());
    verify(mockMemberService, times(1)).findByLonginTypeAndSocialId(any(), any());
  }
}

package com.project.shoppingmall.filter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.dto.auth.AuthUserDetail;
import com.project.shoppingmall.dto.token.AccessTokenData;
import com.project.shoppingmall.service.auth.AuthMemberDetailService;
import com.project.shoppingmall.type.MemberRoleType;
import com.project.shoppingmall.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtFilterTest {
  private JwtFilter target;
  private JwtUtil mockJwtUtil;
  private AuthMemberDetailService mockAuthMemberDetailService;

  @BeforeEach
  public void beforeEach() {
    mockJwtUtil = mock(JwtUtil.class);
    mockAuthMemberDetailService = mock(AuthMemberDetailService.class);
    target = new JwtFilter(mockJwtUtil, mockAuthMemberDetailService);
  }

  @Test
  @DisplayName("doFilterInternal() : 정상흐름")
  public void doFilterInternal_ok() throws ServletException, IOException {
    // given
    // - request 인자 세팅
    MockHttpServletRequest rightRequest = new MockHttpServletRequest();
    String givenAccessToken = "testAccessToken123412341234";
    rightRequest.addHeader("Authorization", "Bearer " + givenAccessToken);

    // - response 인자 세팅
    MockHttpServletResponse rightResponse = new MockHttpServletResponse();

    // - filterchain 인자 세팅
    FilterChain rightFilterChain = mock(FilterChain.class);

    // - jwtUtil.decodeAccessToken() 세팅
    Long givenMemberId = 6L;
    MemberRoleType givenMemberRole = MemberRoleType.ROLE_MEMBER;
    AccessTokenData givenAccessTokenData = new AccessTokenData(givenMemberId, givenMemberRole);
    when(mockJwtUtil.decodeAccessToken(anyString())).thenReturn(givenAccessTokenData);

    // - authUserDetailService.loadUserByUsername() 세팅
    AuthUserDetail givenAuthUserDetail = new AuthUserDetail(givenMemberId, givenMemberRole);
    when(mockAuthMemberDetailService.loadUserByUsername(anyString()))
        .thenReturn(givenAuthUserDetail);

    // - SecurityContextHolder 세팅
    SecurityContext givenSecurityContext = mock(SecurityContext.class);
    SecurityContextHolder.setContext(givenSecurityContext);

    // when
    target.doFilterInternal(rightRequest, rightResponse, rightFilterChain);

    // then
    ArgumentCaptor<String> accessTokenCaptor = ArgumentCaptor.forClass(String.class);
    verify(mockJwtUtil, times(1)).decodeAccessToken(accessTokenCaptor.capture());
    assertEquals(givenAccessToken, accessTokenCaptor.getValue());

    ArgumentCaptor<String> memberIdCaptor = ArgumentCaptor.forClass(String.class);
    verify(mockAuthMemberDetailService, times(1)).loadUserByUsername(memberIdCaptor.capture());
    assertEquals(givenMemberId.toString(), memberIdCaptor.getValue());

    ArgumentCaptor<UsernamePasswordAuthenticationToken> authTokenCaptor =
        ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
    verify(givenSecurityContext, times(1)).setAuthentication(authTokenCaptor.capture());
    assertEquals(givenAuthUserDetail, authTokenCaptor.getValue().getPrincipal());
    assertEquals(
        givenMemberRole.toString(),
        authTokenCaptor.getValue().getAuthorities().iterator().next().getAuthority());
  }

  @Test
  @DisplayName("doFilterInternal() : 형식에 맞지 않는 토큰 입력")
  public void doFilterInternal_incorrectToken() throws ServletException, IOException {
    // given
    // - request 인자 세팅
    MockHttpServletRequest rightRequest = new MockHttpServletRequest();
    String givenAccessToken = "testAccessToken123412341234";
    rightRequest.addHeader("Authorization", "NotBearer " + givenAccessToken);

    // - response 인자 세팅
    MockHttpServletResponse rightResponse = new MockHttpServletResponse();

    // - filterchain 인자 세팅
    FilterChain rightFilterChain = mock(FilterChain.class);

    // - SecurityContextHolder 세팅
    SecurityContext givenSecurityContext = mock(SecurityContext.class);
    SecurityContextHolder.setContext(givenSecurityContext);

    // when
    target.doFilterInternal(rightRequest, rightResponse, rightFilterChain);

    // then
    verify(mockJwtUtil, times(0)).decodeAccessToken(any());
    verify(mockAuthMemberDetailService, times(0)).loadUserByUsername(any());
    verify(givenSecurityContext, times(0)).setAuthentication(any());
  }

  @Test
  @DisplayName("doFilterInternal() : 토큰 미입력")
  public void doFilterInternal_blankToken() throws ServletException, IOException {
    // given
    // - request 인자 세팅
    MockHttpServletRequest rightRequest = new MockHttpServletRequest();

    // - response 인자 세팅
    MockHttpServletResponse rightResponse = new MockHttpServletResponse();

    // - filterchain 인자 세팅
    FilterChain rightFilterChain = mock(FilterChain.class);

    // - SecurityContextHolder 세팅
    SecurityContext givenSecurityContext = mock(SecurityContext.class);
    SecurityContextHolder.setContext(givenSecurityContext);

    // when
    target.doFilterInternal(rightRequest, rightResponse, rightFilterChain);

    // then
    verify(mockJwtUtil, times(0)).decodeAccessToken(any());
    verify(mockAuthMemberDetailService, times(0)).loadUserByUsername(any());
    verify(givenSecurityContext, times(0)).setAuthentication(any());
  }
}

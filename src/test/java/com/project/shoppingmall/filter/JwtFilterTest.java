package com.project.shoppingmall.filter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.dto.auth.AuthManagerDetail;
import com.project.shoppingmall.dto.auth.AuthMemberDetail;
import com.project.shoppingmall.dto.token.AccessTokenData;
import com.project.shoppingmall.service.auth.AuthMemberDetailService;
import com.project.shoppingmall.service_manage.auth.AuthManagerDetailService;
import com.project.shoppingmall.type.ManagerRoleType;
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
  private AuthManagerDetailService mockAuthManagerDetailService;

  @BeforeEach
  public void beforeEach() {
    mockJwtUtil = mock(JwtUtil.class);
    mockAuthMemberDetailService = mock(AuthMemberDetailService.class);
    mockAuthManagerDetailService = mock(AuthManagerDetailService.class);
    target = new JwtFilter(mockJwtUtil, mockAuthMemberDetailService, mockAuthManagerDetailService);
  }

  @Test
  @DisplayName("doFilterInternal() : 정상흐름 - 일반 회원의 토큰 입력")
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
    AccessTokenData givenAccessTokenData =
        new AccessTokenData(givenMemberId, givenMemberRole.toString());
    when(mockJwtUtil.decodeAccessToken(anyString())).thenReturn(givenAccessTokenData);

    // - authUserDetailService.loadUserByUsername() 세팅
    AuthMemberDetail givenAuthMemberDetail = new AuthMemberDetail(givenMemberId, givenMemberRole);
    when(mockAuthMemberDetailService.loadUserByUsername(anyString()))
        .thenReturn(givenAuthMemberDetail);

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
    assertEquals(givenAuthMemberDetail, authTokenCaptor.getValue().getPrincipal());
    assertEquals(
        givenMemberRole.toString(),
        authTokenCaptor.getValue().getAuthorities().iterator().next().getAuthority());
  }

  @Test
  @DisplayName("doFilterInternal() : 관리자의 토큰 입력")
  public void doFilterInternal_manager() throws ServletException, IOException {
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
    Long givenManagerId = 6L;
    ManagerRoleType givenManagerRole = ManagerRoleType.ROLE_COMMON_MANAGER;
    AccessTokenData givenAccessTokenData =
        new AccessTokenData(givenManagerId, givenManagerRole.toString());
    when(mockJwtUtil.decodeAccessToken(anyString())).thenReturn(givenAccessTokenData);

    // - authManagerDetailService.loadUserByUsername() 세팅
    AuthManagerDetail givenAuthManagerDetail =
        new AuthManagerDetail(givenManagerId, givenManagerRole);
    when(mockAuthManagerDetailService.loadUserByUsername(anyString()))
        .thenReturn(givenAuthManagerDetail);

    // - SecurityContextHolder 세팅
    SecurityContext givenSecurityContext = mock(SecurityContext.class);
    SecurityContextHolder.setContext(givenSecurityContext);

    // when
    target.doFilterInternal(rightRequest, rightResponse, rightFilterChain);

    // then
    ArgumentCaptor<String> accessTokenCaptor = ArgumentCaptor.forClass(String.class);
    verify(mockJwtUtil, times(1)).decodeAccessToken(accessTokenCaptor.capture());
    assertEquals(givenAccessToken, accessTokenCaptor.getValue());

    ArgumentCaptor<String> managerIdCaptor = ArgumentCaptor.forClass(String.class);
    verify(mockAuthManagerDetailService, times(1)).loadUserByUsername(managerIdCaptor.capture());
    assertEquals(givenManagerId.toString(), managerIdCaptor.getValue());

    ArgumentCaptor<UsernamePasswordAuthenticationToken> authTokenCaptor =
        ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
    verify(givenSecurityContext, times(1)).setAuthentication(authTokenCaptor.capture());
    assertEquals(givenAuthManagerDetail, authTokenCaptor.getValue().getPrincipal());
    assertEquals(
        givenManagerRole.toString(),
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

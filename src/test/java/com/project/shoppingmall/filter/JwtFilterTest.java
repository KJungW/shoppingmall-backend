package com.project.shoppingmall.filter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.dto.auth.AuthManagerDetail;
import com.project.shoppingmall.dto.auth.AuthMemberDetail;
import com.project.shoppingmall.dto.token.AccessTokenData;
import com.project.shoppingmall.exception.JwtTokenException;
import com.project.shoppingmall.service.auth.AuthMemberDetailService;
import com.project.shoppingmall.service_manage.auth.AuthManagerDetailService;
import com.project.shoppingmall.test_dto.auth.AuthManagerDetailManager;
import com.project.shoppingmall.test_dto.auth.AuthMemberDetailManager;
import com.project.shoppingmall.test_dto.token.AccessTokenManager;
import com.project.shoppingmall.type.ManagerRoleType;
import com.project.shoppingmall.type.MemberRoleType;
import com.project.shoppingmall.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
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

    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("doFilterInternal() : 정상흐름 - 일반 회원의 토큰 입력")
  public void doFilterInternal_ok() throws ServletException, IOException {
    // given
    HttpServletRequest inputRequest = makeJwtAuthMockRequest("Bearer testAccessTokenStr");
    HttpServletResponse inputResponse = new MockHttpServletResponse();
    FilterChain inputFilterChain = mock(FilterChain.class);

    long givenMemberId = 10L;
    MemberRoleType givenMemberRole = MemberRoleType.ROLE_MEMBER;
    AccessTokenData givenAccessTokenData =
        AccessTokenManager.makeMemberToken(givenMemberId, givenMemberRole);
    AuthMemberDetail givenMemberDetail =
        AuthMemberDetailManager.make(givenMemberId, givenMemberRole);

    when(mockJwtUtil.decodeAccessToken(anyString())).thenReturn(givenAccessTokenData);
    when(mockAuthMemberDetailService.loadUserByUsername(anyString())).thenReturn(givenMemberDetail);

    // when
    target.doFilterInternal(inputRequest, inputResponse, inputFilterChain);

    // then
    check_authMemberDetailService_loadUserByUsername(givenMemberId);
    checkMemberDetailInSecurityContext(givenMemberId, givenMemberRole);
  }

  @Test
  @DisplayName("doFilterInternal() : 관리자의 토큰 입력")
  public void doFilterInternal_manager() throws ServletException, IOException {
    // given
    HttpServletRequest inputRequest = makeJwtAuthMockRequest("Bearer testAccessTokenStr");
    HttpServletResponse inputResponse = new MockHttpServletResponse();
    FilterChain inputFilterChain = mock(FilterChain.class);

    long givenManagerId = 10L;
    ManagerRoleType givenMangerRole = ManagerRoleType.ROLE_COMMON_MANAGER;
    AccessTokenData givenAccessTokenData =
        AccessTokenManager.makeManagerToken(givenManagerId, givenMangerRole);
    AuthManagerDetail givenManagerDetail =
        AuthManagerDetailManager.make(givenManagerId, givenMangerRole);

    when(mockJwtUtil.decodeAccessToken(anyString())).thenReturn(givenAccessTokenData);
    when(mockAuthManagerDetailService.loadUserByUsername(anyString()))
        .thenReturn(givenManagerDetail);

    // when
    target.doFilterInternal(inputRequest, inputResponse, inputFilterChain);

    // then
    check_authManagerDetailService_loadUserByUsername(givenManagerId);
    checkManagerDetailInSecurityContext(givenManagerId, givenMangerRole);
  }

  @Test
  @DisplayName("doFilterInternal() : 형식에 맞지 않는 토큰 입력")
  public void doFilterInternal_incorrectToken() throws ServletException, IOException {
    // given
    HttpServletRequest inputRequest = makeJwtAuthMockRequest("xxxx Bearer testAccessTokenStr");
    HttpServletResponse inputResponse = new MockHttpServletResponse();
    FilterChain inputFilterChain = mock(FilterChain.class);

    // when
    target.doFilterInternal(inputRequest, inputResponse, inputFilterChain);

    // then
    checkEmptySecurityContext();
  }

  @Test
  @DisplayName("doFilterInternal() : 토큰 미입력")
  public void doFilterInternal_blankToken() throws ServletException, IOException {
    // given
    HttpServletRequest inputRequest = makeJwtAuthMockRequest("Bearer ");
    HttpServletResponse inputResponse = new MockHttpServletResponse();
    FilterChain inputFilterChain = mock(FilterChain.class);

    when(mockJwtUtil.decodeAccessToken(anyString())).thenThrow(JwtTokenException.class);

    // when
    target.doFilterInternal(inputRequest, inputResponse, inputFilterChain);

    // then
    checkEmptySecurityContext();
  }

  public HttpServletRequest makeJwtAuthMockRequest(String authHeaderContent) {
    MockHttpServletRequest mockRequest = new MockHttpServletRequest();
    mockRequest.addHeader("Authorization", authHeaderContent);
    return mockRequest;
  }

  public void check_authMemberDetailService_loadUserByUsername(long givenMemberId) {
    ArgumentCaptor<String> memberIdCaptor = ArgumentCaptor.forClass(String.class);
    verify(mockAuthMemberDetailService, times(1)).loadUserByUsername(memberIdCaptor.capture());
    assertEquals(String.valueOf(givenMemberId), memberIdCaptor.getValue());
  }

  public void check_authManagerDetailService_loadUserByUsername(long givenManagerId) {
    ArgumentCaptor<String> managerIdCaptor = ArgumentCaptor.forClass(String.class);
    verify(mockAuthManagerDetailService, times(1)).loadUserByUsername(managerIdCaptor.capture());
    assertEquals(String.valueOf(givenManagerId), managerIdCaptor.getValue());
  }

  public void checkMemberDetailInSecurityContext(long memberId, MemberRoleType role) {
    AuthMemberDetail memberDetail =
        (AuthMemberDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    AuthMemberDetailManager.check(memberId, role, memberDetail);
  }

  public void checkEmptySecurityContext() {

    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  public void checkManagerDetailInSecurityContext(long managerId, ManagerRoleType role) {
    AuthManagerDetail mangerDetail =
        (AuthManagerDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    AuthManagerDetailManager.check(managerId, role, mangerDetail);
  }
}

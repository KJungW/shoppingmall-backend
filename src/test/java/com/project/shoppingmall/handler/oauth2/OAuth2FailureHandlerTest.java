package com.project.shoppingmall.handler.oauth2;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.util.ReflectionTestUtils;

class OAuth2FailureHandlerTest {
  private OAuth2FailureHandler target;
  private HttpServletRequest request;
  private HttpServletResponse response;
  private AuthenticationException exception;

  @BeforeEach
  public void beforeEach() {
    target = new OAuth2FailureHandler();
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    exception = mock(AuthenticationException.class);
  }

  @Test
  @DisplayName("OAuth2FailureHandler.onAuthenticationFailure : 정상흐름")
  public void onAuthenticationFailure_ok() throws ServletException, IOException {
    // given
    String givenFailRedirectionUrl = "http://localhost:3000/exit";
    ReflectionTestUtils.setField(target, "loginFailRedirectionUrl", givenFailRedirectionUrl);

    // when
    target.onAuthenticationFailure(request, response, exception);

    // then
    MockHttpServletResponse resultResponse = (MockHttpServletResponse) response;
    assertEquals(302, resultResponse.getStatus());
    assertEquals(givenFailRedirectionUrl, resultResponse.getRedirectedUrl());
  }
}

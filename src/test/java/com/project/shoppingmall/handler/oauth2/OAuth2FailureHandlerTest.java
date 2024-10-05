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
  private final String givenLoginFailRedirectionUrl = "http://localhost:3000/exit";

  @BeforeEach
  public void beforeEach() {
    target = new OAuth2FailureHandler();
    ReflectionTestUtils.setField(target, "loginFailRedirectionUrl", givenLoginFailRedirectionUrl);
  }

  @Test
  @DisplayName("OAuth2FailureHandler.onAuthenticationFailure : 정상흐름")
  public void onAuthenticationFailure_ok() throws ServletException, IOException {
    // given
    HttpServletRequest inputRequest = new MockHttpServletRequest();
    HttpServletResponse inputResponse = new MockHttpServletResponse();
    AuthenticationException inputException = mock(AuthenticationException.class);

    // when
    target.onAuthenticationFailure(inputRequest, inputResponse, inputException);

    // then
    checkResponseResult(inputResponse);
  }

  public void checkResponseResult(HttpServletResponse response) {
    MockHttpServletResponse resultResponse = (MockHttpServletResponse) response;
    assertEquals(302, resultResponse.getStatus());
    assertEquals(givenLoginFailRedirectionUrl, resultResponse.getRedirectedUrl());
  }
}

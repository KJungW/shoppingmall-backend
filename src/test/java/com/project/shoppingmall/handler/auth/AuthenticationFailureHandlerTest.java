package com.project.shoppingmall.handler.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.dto.exception.ErrorResult;
import com.project.shoppingmall.type.ErrorCode;
import com.project.shoppingmall.util.JsonUtil;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

class AuthenticationFailureHandlerTest {
  private AuthenticationFailureHandler target;

  @BeforeEach
  public void beforeEach() {
    target = new AuthenticationFailureHandler();
  }

  @Test
  @DisplayName("commence() : 정상흐름")
  public void commence() throws IOException {
    // given
    // - requeset 인자 세팅
    MockHttpServletRequest rightRequest = new MockHttpServletRequest();
    // - response 인자 세팅
    MockHttpServletResponse rightResponse = new MockHttpServletResponse();
    // - authException 세팅
    AuthenticationException givenAuthException = mock(AuthenticationException.class);

    // when
    target.commence(rightRequest, rightResponse, givenAuthException);

    // then
    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, rightResponse.getStatus());
    assertEquals("application/json;charset=utf-8", rightResponse.getContentType());

    ErrorResult contentInResponse =
        JsonUtil.convertJsonToObject(rightResponse.getContentAsString(), ErrorResult.class);
    assertEquals(ErrorCode.UNAUTHORIZED, contentInResponse.getCode());
  }
}

package com.project.shoppingmall.handler.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.dto.exception.ErrorResult;
import com.project.shoppingmall.type.ErrorCode;
import com.project.shoppingmall.util.JsonUtil;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
    MockHttpServletRequest inputRequest = new MockHttpServletRequest();
    MockHttpServletResponse inputResponse = new MockHttpServletResponse();
    AuthenticationException inputAuthException = mock(AuthenticationException.class);

    // when
    target.commence(inputRequest, inputResponse, inputAuthException);

    // then
    checkCheckResponseResult(inputResponse);
  }

  private void checkCheckResponseResult(MockHttpServletResponse inputResponse)
      throws UnsupportedEncodingException {
    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, inputResponse.getStatus());
    assertEquals("application/json;charset=utf-8", inputResponse.getContentType());
    ErrorResult contentInResponse =
        JsonUtil.convertJsonToObject(inputResponse.getContentAsString(), ErrorResult.class);
    assertEquals(ErrorCode.UNAUTHORIZED, contentInResponse.getCode());
  }
}

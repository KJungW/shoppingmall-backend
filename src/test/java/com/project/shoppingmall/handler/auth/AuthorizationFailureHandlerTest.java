package com.project.shoppingmall.handler.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

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
import org.springframework.security.access.AccessDeniedException;

class AuthorizationFailureHandlerTest {
  private AuthorizationFailureHandler target;

  @BeforeEach
  public void beforeEach() {
    target = new AuthorizationFailureHandler();
  }

  @Test
  @DisplayName("handle() : 정상흐름")
  public void handle_ok() throws IOException {
    // given
    MockHttpServletRequest inputRequest = new MockHttpServletRequest();
    MockHttpServletResponse inputResponse = new MockHttpServletResponse();
    AccessDeniedException inputAccessDeniedException = mock(AccessDeniedException.class);

    // when
    target.handle(inputRequest, inputResponse, inputAccessDeniedException);

    // then
    checkCheckResponseResult(inputResponse);
  }

  private void checkCheckResponseResult(MockHttpServletResponse inputResponse)
      throws UnsupportedEncodingException {
    assertEquals(HttpServletResponse.SC_FORBIDDEN, inputResponse.getStatus());
    assertEquals("application/json;charset=utf-8", inputResponse.getContentType());
    ErrorResult contentInResponse =
        JsonUtil.convertJsonToObject(inputResponse.getContentAsString(), ErrorResult.class);
    assertEquals(ErrorCode.FORBIDDEN, contentInResponse.getCode());
  }
}

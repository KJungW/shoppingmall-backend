package com.project.shoppingmall.handler.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

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
    // - requeset 인자 세팅
    MockHttpServletRequest rightRequest = new MockHttpServletRequest();
    // - response 인자 세팅
    MockHttpServletResponse rightResponse = new MockHttpServletResponse();
    // - authException 세팅
    AccessDeniedException givenAccessDeniedException = mock(AccessDeniedException.class);

    // when
    target.handle(rightRequest, rightResponse, givenAccessDeniedException);

    // then
    assertEquals(HttpServletResponse.SC_FORBIDDEN, rightResponse.getStatus());
    assertEquals("application/json;charset=utf-8", rightResponse.getContentType());

    ErrorResult contentInResponse =
        JsonUtil.convertJsonToObject(rightResponse.getContentAsString(), ErrorResult.class);
    assertEquals(ErrorCode.FORBIDDEN, contentInResponse.getCode());
  }
}

package com.project.shoppingmall.handler.auth;

import com.project.shoppingmall.dto.exception.ErrorResult;
import com.project.shoppingmall.type.ErrorCode;
import com.project.shoppingmall.util.JsonUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationFailureHandler implements AuthenticationEntryPoint {
  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {
    String content =
        JsonUtil.convertObjectToJson(
            new ErrorResult(ErrorCode.UNAUTHORIZED, "계정정보가 올바르지 않습니다. 다시 로그인을 해주세요"));
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json");
    response.setCharacterEncoding("utf-8");
    response.getWriter().write(content);
  }
}

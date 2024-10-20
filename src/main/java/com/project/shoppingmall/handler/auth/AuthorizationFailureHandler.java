package com.project.shoppingmall.handler.auth;

import com.project.shoppingmall.dto.exception.ErrorResult;
import com.project.shoppingmall.type.ErrorCode;
import com.project.shoppingmall.util.JsonUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthorizationFailureHandler implements AccessDeniedHandler {
  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException)
      throws IOException {
    String content =
        JsonUtil.convertObjectToJson(new ErrorResult(ErrorCode.FORBIDDEN, "권한이 없습니다."));
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.setContentType("application/json");
    response.setCharacterEncoding("utf-8");
    response.getWriter().write(content);
  }
}

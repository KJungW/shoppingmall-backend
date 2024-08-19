package com.project.shoppingmall.controller.auth;

import com.project.shoppingmall.controller.auth.input.InputLongManager;
import com.project.shoppingmall.controller.auth.output.OutputLoginManager;
import com.project.shoppingmall.dto.token.RefreshAndAccessToken;
import com.project.shoppingmall.service.auth.AuthManagerTokenService;
import com.project.shoppingmall.util.CookieUtil;
import com.project.shoppingmall.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthManagerController {
  private final AuthManagerTokenService authManagerTokenService;
  private final CookieUtil cookieUtil;
  private final JwtUtil jwtUtil;

  @PostMapping("/manager/login")
  public OutputLoginManager loginManager(
      HttpServletResponse response, @Valid @RequestBody InputLongManager input) {
    RefreshAndAccessToken longinResult =
        authManagerTokenService.longinManager(input.getSerialNumber(), input.getPassword());
    addRefreshTokenInResponse(response, longinResult.getRefreshToken());
    return new OutputLoginManager(longinResult.getAccessToken());
  }

  private void addRefreshTokenInResponse(HttpServletResponse response, String refreshToken) {
    ResponseCookie cookie =
        cookieUtil.createCookie(
            "refresh", refreshToken, (int) (jwtUtil.getRefreshExpirationTimeMs() / 1000));
    response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }
}

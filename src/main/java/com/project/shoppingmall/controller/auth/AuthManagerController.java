package com.project.shoppingmall.controller.auth;

import com.project.shoppingmall.controller.auth.input.InputLongManager;
import com.project.shoppingmall.controller.auth.output.OutputLoginManager;
import com.project.shoppingmall.controller.auth.output.OutputReissueManagerToken;
import com.project.shoppingmall.dto.auth.AuthManagerDetail;
import com.project.shoppingmall.dto.token.RefreshAndAccessToken;
import com.project.shoppingmall.exception.TokenNotFound;
import com.project.shoppingmall.service.auth.AuthManagerTokenService;
import com.project.shoppingmall.util.CookieUtil;
import com.project.shoppingmall.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
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

  @GetMapping("/manager/reissue")
  public OutputReissueManagerToken reissueManagerToken(
      HttpServletRequest request, HttpServletResponse response) {
    String refreshToken = findRefreshCookie(request);
    RefreshAndAccessToken reissueResult =
        authManagerTokenService.reissueRefreshAndAccess(refreshToken);
    addRefreshTokenInResponse(response, reissueResult.getRefreshToken());
    return new OutputReissueManagerToken(reissueResult.getAccessToken());
  }

  @PreAuthorize("hasAnyRole('ROLE_ROOT_MANAGER', 'ROLE_COMMON_MANAGER')")
  @GetMapping("/manager/logout")
  public void logoutManager(HttpServletResponse response) {
    deleteRefreshTokenInResponse(response);
    AuthManagerDetail userDetail =
        (AuthManagerDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    authManagerTokenService.deleteRefreshToken(userDetail.getId());
  }

  private String findRefreshCookie(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    String refreshToken = cookieUtil.findCookie("refresh", cookies);
    if (refreshToken.isEmpty()) throw new TokenNotFound("refresh 토큰이 존재하지 않습니다.");
    return refreshToken;
  }

  private void addRefreshTokenInResponse(HttpServletResponse response, String refreshToken) {
    ResponseCookie cookie =
        cookieUtil.createCookie(
            "refresh", refreshToken, (int) (jwtUtil.getRefreshExpirationTimeMs() / 1000));
    response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }

  private void deleteRefreshTokenInResponse(HttpServletResponse response) {
    ResponseCookie cookie = cookieUtil.createCookie("refresh", "", 0);
    response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }
}

package com.project.shoppingmall.controller.auth;

import com.project.shoppingmall.controller.auth.output.ReissueOutput;
import com.project.shoppingmall.dto.auth.AuthMemberDetail;
import com.project.shoppingmall.dto.token.RefreshAndAccessToken;
import com.project.shoppingmall.exception.TokenNotFound;
import com.project.shoppingmall.service.auth.AuthMemberTokenService;
import com.project.shoppingmall.util.CookieUtil;
import com.project.shoppingmall.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthMemberController {
  private final JwtUtil jwtUtil;
  private final CookieUtil cookieUtil;
  private final AuthMemberTokenService authMemberTokenService;

  @GetMapping("/reissue")
  public ReissueOutput reissue(HttpServletRequest request, HttpServletResponse response) {
    String refreshToken = findRefreshCookie(request);
    RefreshAndAccessToken reissueResult =
        authMemberTokenService.reissueRefreshAndAccess(refreshToken);
    addRefreshTokenInResponse(response, reissueResult.getRefreshToken());
    return new ReissueOutput(reissueResult.getAccessToken());
  }

  @PreAuthorize("hasRole('ROLE_MEMBER')")
  @GetMapping("/logout")
  public void logout(HttpServletResponse response) {
    deleteRefreshTokenInResponse(response);
    AuthMemberDetail userDetail =
        (AuthMemberDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    authMemberTokenService.deleteRefreshToken(userDetail.getId());
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

package com.project.shoppingmall.filter;

import com.project.shoppingmall.dto.token.AccessTokenData;
import com.project.shoppingmall.service.AuthUserDetailService;
import com.project.shoppingmall.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final AuthUserDetailService authUserDetailService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String authHeader = request.getHeader("Authorization");
    try {
      if (checkAuthHeader(authHeader)) {
        String accessToken = authHeader.substring(7);
        AccessTokenData accessTokenData = jwtUtil.decodeAccessToken(accessToken);
        UserDetails userDetails =
            authUserDetailService.loadUserByUsername(accessTokenData.getId().toString());
        UsernamePasswordAuthenticationToken usernamePasswordToken =
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordToken);
      }
    } catch (Exception e) {
      // AuthenticationFailureHandler에서 처리
    }

    filterChain.doFilter(request, response);
  }

  private Boolean checkAuthHeader(String authHeader) {
    return authHeader != null && authHeader.startsWith("Bearer ");
  }
}

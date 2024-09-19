package com.project.shoppingmall.filter;

import com.project.shoppingmall.dto.exception.ErrorResult;
import com.project.shoppingmall.dto.token.AccessTokenData;
import com.project.shoppingmall.exception.JwtTokenException;
import com.project.shoppingmall.final_value.CacheTemplate;
import com.project.shoppingmall.repository.CacheRepository;
import com.project.shoppingmall.type.ErrorCode;
import com.project.shoppingmall.type.ManagerRoleType;
import com.project.shoppingmall.util.JsonUtil;
import com.project.shoppingmall.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class ManagerModeFilter extends OncePerRequestFilter {
  private final CacheRepository cacheRepository;
  private final JwtUtil jwtUtil;

  @Value("${frontend.request_block_by_manager_mode_url}")
  private String requestBlockByManagerModeUrl;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    if (checkPermitUrl(request)) {
      filterChain.doFilter(request, response);
      return;
    }
    boolean isManagerModeOn =
        cacheRepository.getCache(CacheTemplate.makeManagerModeStatusCacheKey()).isPresent();
    if (isManagerModeOn) {
      if (checkRootManagerAccessToken(request)) filterChain.doFilter(request, response);
      else if (checkIsPassableUriInManagerMode(request)) filterChain.doFilter(request, response);
      else makeResponseWithRequestBlockOnManagerModeOn(response);
    } else {
      if (!request.getRequestURI().equals("/product/type")) filterChain.doFilter(request, response);
      else makeResponseWithRequestBlockOnManagerModeOff(response);
    }
  }

  private boolean checkPermitUrl(HttpServletRequest request) {
    String uri = request.getRequestURI();
    if (uri.equals("/health") || uri.equals("/product/types")) return true;
    else return false;
  }

  private boolean checkRootManagerAccessToken(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) return false;

    try {
      String accessToken = authHeader.substring(7);
      AccessTokenData accessTokenData = jwtUtil.decodeAccessToken(accessToken);
      return accessTokenData.getRoleType().equals(ManagerRoleType.ROLE_ROOT_MANAGER.toString());
    } catch (JwtTokenException ex) {
      return false;
    }
  }

  private boolean checkIsPassableUriInManagerMode(HttpServletRequest request) {
    String requestURI = request.getRequestURI();
    return requestURI.equals("/manager/login")
        || requestURI.equals("/manager/reissue")
        || requestURI.equals("/manager/logout");
  }

  private void makeResponseWithRequestBlockOnManagerModeOn(HttpServletResponse response)
      throws IOException {
    response.setStatus(HttpServletResponse.SC_FOUND);
    response.setContentType("application/json; charset=UTF-8");
    ErrorResult errorResult =
        new ErrorResult(
            ErrorCode.BAD_INPUT, "현재 관리자 모드에서 켜져있습니다. 루트관리자 계정의 토큰이 유효한지 URL이 적절한지 확인해주세요");
    String errorResultJson = JsonUtil.convertObjectToJson(errorResult);
    response.getWriter().write(errorResultJson);
    response.sendRedirect(requestBlockByManagerModeUrl);
  }

  private void makeResponseWithRequestBlockOnManagerModeOff(HttpServletResponse response)
      throws IOException {
    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    response.setContentType("application/json; charset=UTF-8");
    ErrorResult errorResult =
        new ErrorResult(ErrorCode.BAD_INPUT, "현재 요청은 관리자 모드에서 실행되어야하는 API 입니다.");
    String errorResultJson = JsonUtil.convertObjectToJson(errorResult);
    response.getWriter().write(errorResultJson);
  }
}

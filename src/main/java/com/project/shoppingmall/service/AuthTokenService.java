package com.project.shoppingmall.service;

import com.project.shoppingmall.dto.token.AccessTokenData;
import com.project.shoppingmall.dto.token.RefreshAndAccessToken;
import com.project.shoppingmall.dto.token.RefreshTokenData;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.exception.JwtTokenException;
import com.project.shoppingmall.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthTokenService {
  private final MemberService memberService;
  private final JwtUtil jwtUtil;

  @Transactional
  public RefreshAndAccessToken reissueRefreshAndAccess(String inputRefreshToken) {
    try {
      RefreshTokenData refreshTokenData = jwtUtil.decodeRefreshToken(inputRefreshToken);
      Long memberId = refreshTokenData.getId();
      Member member =
          memberService
              .findById(memberId)
              .orElseThrow(() -> new DataNotFound("토큰에 해당하는 유저를 찾을 수 없습니다."));
      String dbRefreshToken = member.getToken().getRefresh();
      validationRefreshToken(inputRefreshToken, dbRefreshToken);
      RefreshAndAccessToken refreshAndAccessToken = makeNewRefreshAndAccess(refreshTokenData);
      member.getToken().updateRefresh(refreshAndAccessToken.getRefreshToken());
      return refreshAndAccessToken;

    } catch (Exception ex) {
      throw new JwtTokenException("refresh 토큰이 유효하지 않습니다.");
    }
  }

  private RefreshAndAccessToken makeNewRefreshAndAccess(RefreshTokenData tokenData) {
    AccessTokenData accessTokenData =
        new AccessTokenData(tokenData.getId(), tokenData.getRoleType());
    String newAccessToken = jwtUtil.createAccessToken(accessTokenData);
    String newRefreshToken = jwtUtil.createRefreshToken(tokenData);
    return new RefreshAndAccessToken(newRefreshToken, newAccessToken);
  }

  private void validationRefreshToken(String inputToken, String dbToken) {
    if (!inputToken.equals(dbToken)) {
      throw new JwtTokenException("refresh 토큰이 유효하지 않습니다.");
    }
  }
}

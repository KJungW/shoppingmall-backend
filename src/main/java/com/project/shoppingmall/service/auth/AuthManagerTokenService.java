package com.project.shoppingmall.service.auth;

import com.project.shoppingmall.dto.token.AccessTokenData;
import com.project.shoppingmall.dto.token.RefreshAndAccessToken;
import com.project.shoppingmall.dto.token.RefreshTokenData;
import com.project.shoppingmall.entity.Manager;
import com.project.shoppingmall.entity.ManagerToken;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.exception.JwtTokenException;
import com.project.shoppingmall.service.manager.ManagerService;
import com.project.shoppingmall.util.JwtUtil;
import com.project.shoppingmall.util.PasswordEncoderUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthManagerTokenService {
  private final ManagerService managerService;
  private final JwtUtil jwtUtil;

  @Transactional
  public RefreshAndAccessToken longinManager(String serialNumber, String password) {
    Manager manager =
        managerService
            .findBySerialNumber(serialNumber)
            .orElseThrow(() -> new DataNotFound("해당 SerialNumber에 해당하는 Manager가 존재하지 않습니다."));
    if (!PasswordEncoderUtil.checkPassword(password, manager.getPassword()))
      throw new DataNotFound("입력된 비밀번호가 Manager의 비밀번호와 맞지 않습니다.");

    String refreshToken =
        jwtUtil.createRefreshToken(
            new RefreshTokenData(manager.getId(), manager.getRole().toString()));
    String accessToken =
        jwtUtil.createAccessToken(
            new AccessTokenData(manager.getId(), manager.getRole().toString()));

    ManagerToken managerRefreshToken = new ManagerToken(refreshToken);
    manager.updateRefreshToken(managerRefreshToken);

    return new RefreshAndAccessToken(refreshToken, accessToken);
  }

  @Transactional
  public RefreshAndAccessToken reissueRefreshAndAccess(String inputRefreshToken) {
    try {
      RefreshTokenData refreshTokenData = jwtUtil.decodeRefreshToken(inputRefreshToken);
      Long managerId = refreshTokenData.getId();
      Manager manager =
          managerService
              .findById(managerId)
              .orElseThrow(() -> new DataNotFound("토큰에 해당하는 유저를 찾을 수 없습니다."));

      String dbRefreshToken = manager.getToken().getRefresh();
      validationRefreshToken(inputRefreshToken, dbRefreshToken);

      RefreshAndAccessToken refreshAndAccessToken = makeNewRefreshAndAccess(refreshTokenData);
      manager.getToken().updateRefresh(refreshAndAccessToken.getRefreshToken());
      return refreshAndAccessToken;

    } catch (Exception ex) {
      throw new JwtTokenException("refresh 토큰이 유효하지 않습니다.");
    }
  }

  @Transactional
  public void deleteRefreshToken(Long managerId) {
    Manager manager =
        managerService
            .findById(managerId)
            .orElseThrow(() -> new DataNotFound("토큰에 해당하는 유저를 찾을 수 없습니다."));
    manager.deleteRefreshToken();
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

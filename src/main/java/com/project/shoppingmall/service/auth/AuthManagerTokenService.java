package com.project.shoppingmall.service.auth;

import com.project.shoppingmall.dto.token.AccessTokenData;
import com.project.shoppingmall.dto.token.RefreshAndAccessToken;
import com.project.shoppingmall.dto.token.RefreshTokenData;
import com.project.shoppingmall.entity.Manager;
import com.project.shoppingmall.entity.ManagerToken;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.ManagerRepository;
import com.project.shoppingmall.util.JwtUtil;
import com.project.shoppingmall.util.PasswordEncoderUtil;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthManagerTokenService {
  private final ManagerRepository managerRepository;
  private final JwtUtil jwtUtil;

  @Transactional
  public RefreshAndAccessToken longinManager(String serialNumber, String password) {
    Manager manager =
        findBySerialNumber(serialNumber)
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

  public Optional<Manager> findBySerialNumber(String serialNumber) {
    return managerRepository.findBySerialNumber(serialNumber);
  }
}

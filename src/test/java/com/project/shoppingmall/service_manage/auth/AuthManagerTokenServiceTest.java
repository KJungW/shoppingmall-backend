package com.project.shoppingmall.service_manage.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.dto.token.RefreshAndAccessToken;
import com.project.shoppingmall.dto.token.RefreshTokenData;
import com.project.shoppingmall.entity.Manager;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.exception.JwtTokenException;
import com.project.shoppingmall.service_manage.common.ManagerService;
import com.project.shoppingmall.test_dto.token.RefreshAndAccessTokenManager;
import com.project.shoppingmall.test_entity.manager.ManagerBuilder;
import com.project.shoppingmall.util.JwtUtil;
import java.util.Optional;
import org.junit.jupiter.api.*;

class AuthManagerTokenServiceTest {
  private AuthManagerTokenService target;
  private ManagerService mockManagerService;
  private JwtUtil mockJwtUtil;

  @BeforeEach
  public void beforeEach() {
    mockManagerService = mock(ManagerService.class);
    mockJwtUtil = mock(JwtUtil.class);
    target = new AuthManagerTokenService(mockManagerService, mockJwtUtil);
  }

  @Test
  @DisplayName("longinManager() : 정상흐름")
  public void loginManager_ok() {
    // given
    String inputSerialNumber = "hgkghktyurdfgbxcvvxcv";
    String inputPassword = "12302104s!$%@sdflsdf";

    Manager givenManager = ManagerBuilder.make(10L, inputSerialNumber, inputPassword);
    String givenRefreshToken = "slkdfjwlrlsmdvcxzvwer";
    String givenAccessToken = "xcvcxzvwerqerwqvsdfsdf";

    when(mockManagerService.findBySerialNumber(anyString())).thenReturn(Optional.of(givenManager));
    when(mockJwtUtil.createRefreshToken(any())).thenReturn(givenRefreshToken);
    when(mockJwtUtil.createAccessToken(any())).thenReturn(givenAccessToken);

    // when
    RefreshAndAccessToken result = target.longinManager(inputSerialNumber, inputPassword);

    // then
    RefreshAndAccessTokenManager.check(givenRefreshToken, givenAccessToken, result);
    checkRefreshTokenInManager(givenRefreshToken, givenManager);
  }

  @Test
  @DisplayName("longinManager() : 비밀번호가 올바르지 않을 경우")
  public void loginManager_invalidPassword() {
    // given
    String inputSerialNumber = "hgkghktyurdfgbxcvvxcv";
    String inputPassword = "12302104s!$%@sdflsdf";

    String otherPassword = "2312030210Lsdfdsf";
    Manager givenManager = ManagerBuilder.make(10L, inputSerialNumber, otherPassword);
    String givenRefreshToken = "slkdfjwlrlsmdvcxzvwer";
    String givenAccessToken = "xcvcxzvwerqerwqvsdfsdf";

    when(mockManagerService.findBySerialNumber(anyString())).thenReturn(Optional.of(givenManager));
    when(mockJwtUtil.createRefreshToken(any())).thenReturn(givenRefreshToken);
    when(mockJwtUtil.createAccessToken(any())).thenReturn(givenAccessToken);

    // when then
    assertThrows(DataNotFound.class, () -> target.longinManager(inputSerialNumber, inputPassword));
  }

  @Test
  @DisplayName("reissueRefreshAndAccess() : 정상흐름")
  public void reissueRefreshAndAccess_ok() {
    // given
    String inputRefreshToken = "testInputRefreshToken";

    Manager givenManager = ManagerBuilder.make(10L, inputRefreshToken);
    RefreshTokenData givenInputRefreshTokenData =
        new RefreshTokenData(givenManager.getId(), givenManager.getRole().toString());
    String givenNewRefreshToken = "testNewRefreshToken";
    String givenNewAccessToken = "testNewAccessToken";

    when(mockJwtUtil.decodeRefreshToken(anyString())).thenReturn(givenInputRefreshTokenData);
    when(mockManagerService.findById(anyLong())).thenReturn(Optional.of(givenManager));
    when(mockJwtUtil.createRefreshToken(any())).thenReturn(givenNewRefreshToken);
    when(mockJwtUtil.createAccessToken(any())).thenReturn(givenNewAccessToken);

    // when
    RefreshAndAccessToken result = target.reissueRefreshAndAccess(inputRefreshToken);

    // then
    RefreshAndAccessTokenManager.check(givenNewRefreshToken, givenNewAccessToken, result);
    checkRefreshTokenInManager(givenNewRefreshToken, givenManager);
  }

  @Test
  @DisplayName("reissueRefreshAndAccess() : 조회된 관리자 없음")
  public void reissueRefreshAndAccess_noManager() {
    // given
    String inputRefreshToken = "testInputRefreshToken";

    Manager givenManager = ManagerBuilder.make(10L, inputRefreshToken);
    RefreshTokenData givenInputRefreshTokenData =
        new RefreshTokenData(givenManager.getId(), givenManager.getRole().toString());

    when(mockJwtUtil.decodeRefreshToken(anyString())).thenReturn(givenInputRefreshTokenData);
    when(mockManagerService.findById(anyLong())).thenReturn(Optional.empty());

    assertThrows(JwtTokenException.class, () -> target.reissueRefreshAndAccess(inputRefreshToken));
  }

  @Test
  @DisplayName("deleteRefreshToken : 정상흐름")
  public void deleteRefreshToken_ok() {
    // given
    long inputManagerId = 10L;

    Manager givenManager = ManagerBuilder.make(inputManagerId, "test fresh token");

    when(mockManagerService.findById(anyLong())).thenReturn(Optional.of(givenManager));

    // when
    target.deleteRefreshToken(inputManagerId);

    // then
    checkNullTokenInManager(givenManager);
  }

  @Test
  @DisplayName("deleteRefreshToken : 조회된 관리자가 없음")
  public void deleteRefreshToken_noManager() {
    // given
    long inputManagerId = 10L;

    when(mockManagerService.findById(anyLong())).thenReturn(Optional.empty());

    // when then
    assertThrows(DataNotFound.class, () -> target.deleteRefreshToken(inputManagerId));
  }

  public void checkRefreshTokenInManager(String givenRefreshToken, Manager target) {
    assertEquals(givenRefreshToken, target.getToken().getRefresh());
  }

  public void checkNullTokenInManager(Manager target) {
    assertNull(target.getToken());
  }
}

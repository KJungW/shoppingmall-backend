package com.project.shoppingmall.service.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.dto.token.RefreshAndAccessToken;
import com.project.shoppingmall.dto.token.RefreshTokenData;
import com.project.shoppingmall.entity.Manager;
import com.project.shoppingmall.entity.ManagerToken;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.exception.JwtTokenException;
import com.project.shoppingmall.service.manager.ManagerService;
import com.project.shoppingmall.testdata.ManagerBuilder;
import com.project.shoppingmall.type.ManagerRoleType;
import com.project.shoppingmall.util.JwtUtil;
import com.project.shoppingmall.util.PasswordEncoderUtil;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

class AuthManagerTokenServiceTest {
  private AuthManagerTokenService target;
  private ManagerService mockManagerService;
  private JwtUtil mockJwtUtil;
  private static MockedStatic<PasswordEncoderUtil> mockPasswordEncoderUtil;

  @BeforeEach
  public void beforeEach() {
    mockManagerService = mock(ManagerService.class);
    mockJwtUtil = mock(JwtUtil.class);
    target = new AuthManagerTokenService(mockManagerService, mockJwtUtil);

    mockPasswordEncoderUtil = mockStatic(PasswordEncoderUtil.class);
  }

  @AfterEach
  public void afterEach() {
    mockPasswordEncoderUtil.close();
  }

  @Test
  @DisplayName("longinManager() : 정상흐름")
  public void loginManager_ok() {
    // given
    // - 인자세팅
    String givenSerialNumber = "hgkghktyurdfgbxcvvxcv";
    String givenPassword = "12302104s!$%@sdflsdf";

    // - managerRepository.findBySerialNumber() 세팅
    long givenManagerId = 10L;
    ManagerRoleType givenManagerRoleType = ManagerRoleType.ROLE_COMMON_MANAGER;
    Manager givenManager = ManagerBuilder.fullData().build();
    ReflectionTestUtils.setField(givenManager, "id", givenManagerId);
    ReflectionTestUtils.setField(givenManager, "role", givenManagerRoleType);
    when(mockManagerService.findBySerialNumber(anyString())).thenReturn(Optional.of(givenManager));

    // - PasswordEncoderUtil.checkPassword() 세팅
    mockPasswordEncoderUtil
        .when(() -> PasswordEncoderUtil.checkPassword(any(), any()))
        .thenReturn(true);

    // - jwtUtil.createRefreshToken() 세팅
    String givenRefreshToken = "slkdfjwlrlsmdvcxzvwer";
    when(mockJwtUtil.createRefreshToken(any())).thenReturn(givenRefreshToken);

    // - jwtUtil.createAccessToken() 세팅
    String givenAccessToken = "xcvcxzvwerqerwqvsdfsdf";
    when(mockJwtUtil.createAccessToken(any())).thenReturn(givenAccessToken);

    // when
    RefreshAndAccessToken resultTokens = target.longinManager(givenSerialNumber, givenPassword);

    // then
    assertEquals(givenRefreshToken, resultTokens.getRefreshToken());
    assertEquals(givenAccessToken, resultTokens.getAccessToken());
    assertEquals(givenRefreshToken, givenManager.getToken().getRefresh());
  }

  @Test
  @DisplayName("longinManager() : 비밀번호가 올바르지 않을 경우")
  public void loginManager_invalidPassword() {
    // given
    // - 인자세팅
    String givenSerialNumber = "hgkghktyurdfgbxcvvxcv";
    String givenPassword = "12302104s!$%@sdflsdf";

    // - managerRepository.findBySerialNumber() 세팅
    long givenManagerId = 10L;
    ManagerRoleType givenManagerRoleType = ManagerRoleType.ROLE_COMMON_MANAGER;
    Manager givenManager = ManagerBuilder.fullData().build();
    ReflectionTestUtils.setField(givenManager, "id", givenManagerId);
    ReflectionTestUtils.setField(givenManager, "role", givenManagerRoleType);
    when(mockManagerService.findBySerialNumber(anyString())).thenReturn(Optional.of(givenManager));

    // - PasswordEncoderUtil.checkPassword() 세팅
    mockPasswordEncoderUtil
        .when(() -> PasswordEncoderUtil.checkPassword(any(), any()))
        .thenReturn(false);

    // when then
    assertThrows(DataNotFound.class, () -> target.longinManager(givenSerialNumber, givenPassword));
  }

  @Test
  @DisplayName("reissueRefreshAndAccess() : 정상흐름")
  public void reissueRefreshAndAccess_ok() {
    // given
    String givenInputRefreshToken = "testInputRefreshToken";

    long givenMangerId = 10L;
    ManagerRoleType givenRoleType = ManagerRoleType.ROLE_COMMON_MANAGER;
    RefreshTokenData givenInputRefreshTokenData =
        new RefreshTokenData(givenMangerId, givenRoleType.toString());
    when(mockJwtUtil.decodeRefreshToken(anyString())).thenReturn(givenInputRefreshTokenData);

    Manager givenManager = ManagerBuilder.fullData().build();
    ManagerToken givenManagerToken = new ManagerToken(givenInputRefreshToken);
    ReflectionTestUtils.setField(givenManager, "id", givenMangerId);
    ReflectionTestUtils.setField(givenManager, "token", givenManagerToken);
    when(mockManagerService.findById(anyLong())).thenReturn(Optional.of(givenManager));

    String givenNewRefreshToken = "testNewRefreshToken";
    when(mockJwtUtil.createRefreshToken(any())).thenReturn(givenNewRefreshToken);

    String givenNewAccessToken = "testNewAccessToken";
    when(mockJwtUtil.createAccessToken(any())).thenReturn(givenNewAccessToken);

    // when
    RefreshAndAccessToken reissueResult = target.reissueRefreshAndAccess(givenInputRefreshToken);

    // then
    assertEquals(givenNewRefreshToken, reissueResult.getRefreshToken());
    assertEquals(givenNewAccessToken, reissueResult.getAccessToken());
    assertEquals(givenNewRefreshToken, givenManager.getToken().getRefresh());
  }

  @Test
  @DisplayName("reissueRefreshAndAccess() : 조회된 관리자 없음")
  public void reissueRefreshAndAccess_noManager() {
    // given
    String givenInputRefreshToken = "testInputRefreshToken";

    long givenMangerId = 10L;
    ManagerRoleType givenRoleType = ManagerRoleType.ROLE_COMMON_MANAGER;
    RefreshTokenData givenInputRefreshTokenData =
        new RefreshTokenData(givenMangerId, givenRoleType.toString());
    when(mockJwtUtil.decodeRefreshToken(anyString())).thenReturn(givenInputRefreshTokenData);

    when(mockManagerService.findById(anyLong())).thenReturn(Optional.empty());

    assertThrows(
        JwtTokenException.class, () -> target.reissueRefreshAndAccess(givenInputRefreshToken));
  }

  @Test
  @DisplayName("deleteRefreshToken : 정상흐름")
  public void deleteRefreshToken_ok() {
    // given
    long givenManagerId = 10L;

    ManagerToken givenManagerToken = new ManagerToken("test fresh token");
    Manager givenManager = ManagerBuilder.fullData().build();
    ReflectionTestUtils.setField(givenManager, "id", givenManagerId);
    ReflectionTestUtils.setField(givenManager, "token", givenManagerToken);
    when(mockManagerService.findById(anyLong())).thenReturn(Optional.of(givenManager));

    // when
    target.deleteRefreshToken(givenManagerId);

    // then
    assertNull(givenManager.getToken());
  }

  @Test
  @DisplayName("deleteRefreshToken : 조회된 관리자가 없음")
  public void deleteRefreshToken_noManager() {
    // given
    long givenManagerId = 10L;

    when(mockManagerService.findById(anyLong())).thenReturn(Optional.empty());

    // when then
    assertThrows(DataNotFound.class, () -> target.deleteRefreshToken(givenManagerId));
  }
}

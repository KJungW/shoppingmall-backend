package com.project.shoppingmall.service.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.dto.token.RefreshAndAccessToken;
import com.project.shoppingmall.entity.Manager;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.ManagerRepository;
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
  private ManagerRepository mockManagerRepository;
  private JwtUtil mockJwtUtil;
  private static MockedStatic<PasswordEncoderUtil> mockPasswordEncoderUtil;

  @BeforeEach
  public void beforeEach() {
    mockManagerRepository = mock(ManagerRepository.class);
    mockJwtUtil = mock(JwtUtil.class);
    target = new AuthManagerTokenService(mockManagerRepository, mockJwtUtil);

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
    ManagerRoleType givenManagerRoleType = ManagerRoleType.ROLE_COMMON_MANGER;
    Manager givenManager = ManagerBuilder.fullData().build();
    ReflectionTestUtils.setField(givenManager, "id", givenManagerId);
    ReflectionTestUtils.setField(givenManager, "role", givenManagerRoleType);
    when(mockManagerRepository.findBySerialNumber(anyString()))
        .thenReturn(Optional.of(givenManager));

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
    ManagerRoleType givenManagerRoleType = ManagerRoleType.ROLE_COMMON_MANGER;
    Manager givenManager = ManagerBuilder.fullData().build();
    ReflectionTestUtils.setField(givenManager, "id", givenManagerId);
    ReflectionTestUtils.setField(givenManager, "role", givenManagerRoleType);
    when(mockManagerRepository.findBySerialNumber(anyString()))
        .thenReturn(Optional.of(givenManager));

    // - PasswordEncoderUtil.checkPassword() 세팅
    mockPasswordEncoderUtil
        .when(() -> PasswordEncoderUtil.checkPassword(any(), any()))
        .thenReturn(false);

    // when then
    assertThrows(DataNotFound.class, () -> target.longinManager(givenSerialNumber, givenPassword));
  }
}

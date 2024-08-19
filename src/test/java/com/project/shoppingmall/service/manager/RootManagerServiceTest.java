package com.project.shoppingmall.service.manager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.dto.manager.MakeManagerResult;
import com.project.shoppingmall.entity.Manager;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.exception.ServerLogicError;
import com.project.shoppingmall.repository.ManagerRepository;
import com.project.shoppingmall.testdata.ManagerBuilder;
import com.project.shoppingmall.type.ManagerRoleType;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class RootManagerServiceTest {
  private RootManagerService target;
  private ManagerRepository mockManagerRepository;

  @BeforeEach
  public void beforeEach() {
    this.mockManagerRepository = mock(ManagerRepository.class);
    this.target = new RootManagerService(mockManagerRepository);
  }

  @Test
  @DisplayName("makeManager() : 정상흐름")
  public void makeManager_ok() {
    // given
    long givenRootManagerId = 20L;

    Manager givenRootManager = ManagerBuilder.fullData().build();
    ReflectionTestUtils.setField(givenRootManager, "id", givenRootManagerId);
    ReflectionTestUtils.setField(givenRootManager, "role", ManagerRoleType.ROLE_ROOT_MANAGER);
    when(mockManagerRepository.findRootManger()).thenReturn(Optional.of(givenRootManager));

    // when
    MakeManagerResult makeManagerResult = target.makeManager(givenRootManagerId);

    // then
    ArgumentCaptor<Manager> managerCaptor = ArgumentCaptor.forClass(Manager.class);
    verify(mockManagerRepository, times(1)).save(managerCaptor.capture());

    Manager argumentManager = managerCaptor.getValue();
    assertNotNull(argumentManager);
    assertNotNull(argumentManager.getSerialNumber());
    assertNotNull(argumentManager.getPassword());
    assertEquals(ManagerRoleType.ROLE_COMMON_MANAGER, argumentManager.getRole());

    assertEquals(argumentManager.getId(), makeManagerResult.getCreatedManagerId());
    assertEquals(argumentManager.getSerialNumber(), makeManagerResult.getSerialNumber());
    assertNotNull(makeManagerResult.getPassword());
  }

  @Test
  @DisplayName("makeManager() : 루트 관리자가 조회되지 않음")
  public void makeManager_noRootManager() {
    // given
    long givenRootManagerId = 20L;

    when(mockManagerRepository.findRootManger()).thenReturn(Optional.empty());

    // when then
    assertThrows(ServerLogicError.class, () -> target.makeManager(givenRootManagerId));
  }

  @Test
  @DisplayName("makeManager() : 일반 관리자가 조회되지 않음")
  public void makeManager_commonManager() {
    // given
    long givenRootManagerId = 20L;

    Manager givenRootManager = ManagerBuilder.fullData().build();
    ReflectionTestUtils.setField(givenRootManager, "id", givenRootManagerId);
    ReflectionTestUtils.setField(givenRootManager, "role", ManagerRoleType.ROLE_COMMON_MANAGER);
    when(mockManagerRepository.findRootManger()).thenReturn(Optional.of(givenRootManager));

    // when then
    assertThrows(ServerLogicError.class, () -> target.makeManager(givenRootManagerId));
  }

  @Test
  @DisplayName("makeManager() : 조회된 루트 관리자 ID와 입력 ID가 일치하지 않음")
  public void makeManager_notEqualId() {
    // given
    long wrongManagerId = 20L;

    long realMangerId = 30L;
    Manager givenRootManager = ManagerBuilder.fullData().build();
    ReflectionTestUtils.setField(givenRootManager, "id", realMangerId);
    ReflectionTestUtils.setField(givenRootManager, "role", ManagerRoleType.ROLE_ROOT_MANAGER);
    when(mockManagerRepository.findRootManger()).thenReturn(Optional.of(givenRootManager));

    // when
    assertThrows(DataNotFound.class, () -> target.makeManager(wrongManagerId));
  }
}

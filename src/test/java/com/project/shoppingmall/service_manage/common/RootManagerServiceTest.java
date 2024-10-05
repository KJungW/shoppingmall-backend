package com.project.shoppingmall.service_manage.common;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.dto.manager.MakeManagerResult;
import com.project.shoppingmall.entity.Manager;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.exception.ServerLogicError;
import com.project.shoppingmall.repository.CacheRepository;
import com.project.shoppingmall.repository.ManagerRepository;
import com.project.shoppingmall.test_dto.manager.MakeManagerResultManager;
import com.project.shoppingmall.test_entity.manager.ManagerBuilder;
import com.project.shoppingmall.test_entity.manager.ManagerChecker;
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
  private CacheRepository mockCacheRepository;

  @BeforeEach
  public void beforeEach() {
    this.mockManagerRepository = mock(ManagerRepository.class);
    this.mockCacheRepository = mock(CacheRepository.class);
    this.target = new RootManagerService(mockManagerRepository, mockCacheRepository);
  }

  @Test
  @DisplayName("makeManager() : 정상흐름")
  public void makeManager_ok() {
    // given
    long inputRootManagerId = 20L;

    Manager givenRootManager =
        ManagerBuilder.make(inputRootManagerId, ManagerRoleType.ROLE_ROOT_MANAGER);
    Long givenNewManagerId = 13021L;

    when(mockManagerRepository.findRootManger()).thenReturn(Optional.of(givenRootManager));
    when(mockManagerRepository.save(any(Manager.class)))
        .thenAnswer(
            invocation -> {
              Manager manager = invocation.getArgument(0);
              ReflectionTestUtils.setField(manager, "id", givenNewManagerId);
              return manager;
            });

    // when
    MakeManagerResult result = target.makeManager(inputRootManagerId);

    // then
    Manager capturedManager = getParam_managerRepository_save();
    ManagerChecker.checkCommonManager(capturedManager);
    MakeManagerResultManager.check(capturedManager, result);
  }

  @Test
  @DisplayName("makeManager() : 루트 관리자가 조회되지 않음")
  public void makeManager_noRootManager() {
    // given
    long inputRootManagerId = 20L;

    when(mockManagerRepository.findRootManger()).thenReturn(Optional.empty());

    // when then
    assertThrows(ServerLogicError.class, () -> target.makeManager(inputRootManagerId));
  }

  @Test
  @DisplayName("makeManager() : 일반 관리자가 조회됨")
  public void makeManager_commonManager() {
    // given
    long inputRootManagerId = 20L;

    Manager givenRootManager =
        ManagerBuilder.make(inputRootManagerId, ManagerRoleType.ROLE_COMMON_MANAGER);

    when(mockManagerRepository.findRootManger()).thenReturn(Optional.of(givenRootManager));

    // when then
    assertThrows(ServerLogicError.class, () -> target.makeManager(inputRootManagerId));
  }

  @Test
  @DisplayName("makeManager() : 조회된 루트 관리자 ID와 입력 ID가 일치하지 않음")
  public void makeManager_notEqualId() {
    // given
    long inputRootManagerId = 20L;

    Manager givenRootManager = ManagerBuilder.make(4023L, ManagerRoleType.ROLE_ROOT_MANAGER);

    when(mockManagerRepository.findRootManger()).thenReturn(Optional.of(givenRootManager));

    // when
    assertThrows(DataNotFound.class, () -> target.makeManager(inputRootManagerId));
  }

  public Manager getParam_managerRepository_save() {
    ArgumentCaptor<Manager> managerCaptor = ArgumentCaptor.forClass(Manager.class);
    verify(mockManagerRepository, times(1)).save(managerCaptor.capture());
    return managerCaptor.getValue();
  }
}

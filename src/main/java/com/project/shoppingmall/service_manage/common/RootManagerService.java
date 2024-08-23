package com.project.shoppingmall.service_manage.common;

import com.project.shoppingmall.dto.manager.MakeManagerResult;
import com.project.shoppingmall.entity.Manager;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.exception.ServerLogicError;
import com.project.shoppingmall.repository.ManagerRepository;
import com.project.shoppingmall.type.ManagerRoleType;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RootManagerService {
  private final ManagerRepository managerRepository;

  @Transactional
  public MakeManagerResult makeManager(long rootManagerId) {
    Manager rootManager =
        findRootManager().orElseThrow(() -> new ServerLogicError("현재 서버에 루트 관리자 계정이 존재하지 않습니다."));

    if (rootManager.getRole() != ManagerRoleType.ROLE_ROOT_MANAGER)
      throw new ServerLogicError("루트 관리자 계정이 아닌 일반 관리자 계정이 조회되었습니다.");

    if (!rootManager.getId().equals(rootManagerId)) {
      throw new DataNotFound("올바르지 않은 루트 관리자계정 ID가 입력되었습니다.");
    }

    String newSerialNumber = UUID.randomUUID().toString().replace("-", "");
    String newPassword = UUID.randomUUID().toString().replace("-", "");
    Manager newCommonManager =
        Manager.builder()
            .serialNumber(newSerialNumber)
            .password(newPassword)
            .role(ManagerRoleType.ROLE_COMMON_MANAGER)
            .build();
    managerRepository.save(newCommonManager);

    return new MakeManagerResult(
        newCommonManager.getId(), newCommonManager.getSerialNumber(), newPassword);
  }

  public Optional<Manager> findRootManager() {
    return managerRepository.findRootManger();
  }
}

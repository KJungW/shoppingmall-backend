package com.project.shoppingmall.service_manage.common;

import com.project.shoppingmall.entity.Manager;
import com.project.shoppingmall.repository.ManagerRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ManagerService {
  private final ManagerRepository managerRepository;

  public Optional<Manager> findById(long managerId) {
    return managerRepository.findById(managerId);
  }

  public Optional<Manager> findBySerialNumber(String serialNumber) {
    return managerRepository.findBySerialNumber(serialNumber);
  }
}

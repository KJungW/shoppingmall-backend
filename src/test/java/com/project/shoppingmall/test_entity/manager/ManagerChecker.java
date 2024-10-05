package com.project.shoppingmall.test_entity.manager;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.project.shoppingmall.entity.Manager;
import com.project.shoppingmall.type.ManagerRoleType;

public class ManagerChecker {
  public static void checkCommonManager(Manager target) {
    assertNotNull(target.getSerialNumber());
    assertFalse(target.getSerialNumber().isBlank());
    assertNotNull(target.getPassword());
    assertFalse(target.getPassword().isBlank());
    assertEquals(ManagerRoleType.ROLE_COMMON_MANAGER, target.getRole());
  }
}

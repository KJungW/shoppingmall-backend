package com.project.shoppingmall.test_dto.auth;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.dto.auth.AuthManagerDetail;
import com.project.shoppingmall.entity.Manager;
import com.project.shoppingmall.type.ManagerRoleType;

public class AuthManagerDetailManager {
  public static AuthManagerDetail make(long managerId, ManagerRoleType role) {
    return new AuthManagerDetail(managerId, role);
  }

  public static void check(Manager manager, AuthManagerDetail target) {
    assertEquals(manager.getId(), target.getId());
    assertEquals(manager.getRole(), target.getRole());
  }

  public static void check(long managerId, ManagerRoleType role, AuthManagerDetail target) {
    assertEquals(managerId, target.getId());
    assertEquals(role, target.getRole());
  }
}

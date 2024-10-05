package com.project.shoppingmall.test_dto.manager;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.dto.manager.MakeManagerResult;
import com.project.shoppingmall.entity.Manager;
import com.project.shoppingmall.util.PasswordEncoderUtil;

public class MakeManagerResultManager {
  public static void check(Manager manager, MakeManagerResult target) {
    assertEquals(manager.getId(), target.getCreatedManagerId());
    assertEquals(manager.getSerialNumber(), target.getSerialNumber());
    assertTrue(PasswordEncoderUtil.checkPassword(target.getPassword(), manager.getPassword()));
  }
}

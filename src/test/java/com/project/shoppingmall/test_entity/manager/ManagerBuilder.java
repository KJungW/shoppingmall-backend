package com.project.shoppingmall.test_entity.manager;

import com.project.shoppingmall.entity.Manager;
import com.project.shoppingmall.entity.ManagerToken;
import com.project.shoppingmall.type.ManagerRoleType;
import org.springframework.test.util.ReflectionTestUtils;

public class ManagerBuilder {
  public static Manager.ManagerBuilder fullData() {
    return Manager.builder()
        .serialNumber("sljcxiov#@$@1Df")
        .password("fwoeijcxlkvjlkmlksdfi234124")
        .role(ManagerRoleType.ROLE_COMMON_MANAGER);
  }

  public static Manager make(long id) {
    Manager manager = fullData().build();
    ReflectionTestUtils.setField(manager, "id", id);
    return manager;
  }

  public static Manager make(long id, ManagerRoleType role) {
    Manager manager = fullData().role(role).build();
    ReflectionTestUtils.setField(manager, "id", id);
    return manager;
  }

  public static Manager make(long id, String serialNumber, String password) {
    Manager manager = fullData().serialNumber(serialNumber).password(password).build();
    ReflectionTestUtils.setField(manager, "id", id);
    return manager;
  }

  public static Manager make(long id, String refreshToken) {
    Manager manager = fullData().build();
    ReflectionTestUtils.setField(manager, "id", id);
    ManagerToken managerToken = new ManagerToken(refreshToken);
    manager.updateRefreshToken(managerToken);
    return manager;
  }
}

package com.project.shoppingmall.testdata;

import com.project.shoppingmall.entity.Manager;
import com.project.shoppingmall.type.ManagerRoleType;

public class ManagerBuilder {
  public static Manager.ManagerBuilder fullData() {
    return Manager.builder()
        .serialNumber("sljcxiov#@$@1Df")
        .password("fwoeijcxlkvjlkmlksdfi234124")
        .role(ManagerRoleType.ROLE_COMMON_MANGER);
  }
}

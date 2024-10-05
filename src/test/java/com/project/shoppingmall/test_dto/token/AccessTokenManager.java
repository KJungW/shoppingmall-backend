package com.project.shoppingmall.test_dto.token;

import com.project.shoppingmall.dto.token.AccessTokenData;
import com.project.shoppingmall.type.ManagerRoleType;
import com.project.shoppingmall.type.MemberRoleType;

public class AccessTokenManager {
  public static AccessTokenData makeMemberToken(long memberId, MemberRoleType role) {
    return new AccessTokenData(memberId, role.toString());
  }

  public static AccessTokenData makeManagerToken(long managerId, ManagerRoleType role) {
    return new AccessTokenData(managerId, role.toString());
  }
}

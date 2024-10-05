package com.project.shoppingmall.test_dto.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.project.shoppingmall.dto.auth.AuthMemberDetail;
import com.project.shoppingmall.type.MemberRoleType;

public class AuthMemberDetailManager {
  public static AuthMemberDetail make(long memberId, MemberRoleType role) {
    return new AuthMemberDetail(memberId, role);
  }

  public static void check(long memberId, MemberRoleType roleType, AuthMemberDetail target) {
    assertEquals(memberId, target.getId());
    assertEquals(roleType, target.getRole());
  }
}

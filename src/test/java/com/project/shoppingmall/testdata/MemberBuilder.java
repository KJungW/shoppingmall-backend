package com.project.shoppingmall.testdata;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.MemberToken;
import com.project.shoppingmall.type.LoginType;
import com.project.shoppingmall.type.MemberRoleType;
import java.util.UUID;

public class MemberBuilder {
  public static Member.MemberBuilder fullData() {
    return Member.builder()
        .loginType(LoginType.NAVER)
        .socialId("sdfx413cv-033124")
        .nickName("Kim")
        .email(UUID.randomUUID() + "@naver.com")
        .profileImageUrl(null)
        .profileImageDownLoadUrl(null)
        .role(MemberRoleType.ROLE_MEMBER)
        .isBan(false)
        .token(new MemberToken("refreshTokenString1111222333"));
  }
}

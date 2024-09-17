package com.project.shoppingmall.testdata;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.MemberToken;
import com.project.shoppingmall.type.LoginType;
import com.project.shoppingmall.type.MemberRoleType;
import java.util.UUID;
import org.springframework.test.util.ReflectionTestUtils;

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

  public static Member makeMember(long givenMemberId, LoginType loginType) {
    Member givenMember =
        MemberBuilder.fullData()
            .loginType(loginType)
            .nickName("testNickName")
            .email(UUID.randomUUID() + "@naver.com")
            .password("tempPassword")
            .role(MemberRoleType.ROLE_MEMBER)
            .isBan(false)
            .build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);
    return givenMember;
  }

  public static Member makeMember(long givenMemberId, LoginType loginType, String givenEmail) {
    Member givenMember =
        MemberBuilder.fullData()
            .loginType(loginType)
            .nickName("tempNicKName")
            .email(givenEmail)
            .password("tempPassword")
            .role(MemberRoleType.ROLE_MEMBER)
            .isBan(false)
            .build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);
    return givenMember;
  }

  public static Member makeMember(
      long givenMemberId, LoginType loginType, String givenEmail, String password) {
    Member givenMember =
        MemberBuilder.fullData()
            .loginType(loginType)
            .nickName("tempNicKName")
            .email(givenEmail)
            .password(password)
            .role(MemberRoleType.ROLE_MEMBER)
            .isBan(false)
            .build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);
    return givenMember;
  }

  public static Member makeMemberWithProfileImage(
      long givenMemberId,
      LoginType loginType,
      String profileImageUri,
      String profileImageDownloadUrl) {
    Member givenMember =
        MemberBuilder.fullData()
            .loginType(loginType)
            .nickName("tempNicKName")
            .email(UUID.randomUUID() + "@naver.com")
            .password("sfwer41243!!!")
            .role(MemberRoleType.ROLE_MEMBER)
            .profileImageUrl(profileImageUri)
            .profileImageDownLoadUrl(profileImageDownloadUrl)
            .isBan(false)
            .build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);
    return givenMember;
  }

  public static Member makeMemberWithAccountNumber(
      long givenMemberId, LoginType loginType, String accountNumber) {
    Member givenMember =
        MemberBuilder.fullData()
            .loginType(loginType)
            .nickName("tempNicKName")
            .email(UUID.randomUUID() + "@naver.com")
            .password("sfwer41243!!!")
            .role(MemberRoleType.ROLE_MEMBER)
            .isBan(false)
            .build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);
    givenMember.registerAccount(accountNumber);
    return givenMember;
  }
}

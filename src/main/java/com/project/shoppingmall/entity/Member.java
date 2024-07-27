package com.project.shoppingmall.entity;

import com.project.shoppingmall.type.LoginType;
import com.project.shoppingmall.type.MemberRoleType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
  @Id @GeneratedValue private Long id;

  @Enumerated(value = EnumType.STRING)
  LoginType loginType;

  String socialId;

  String nickName;

  String email;

  String profileImageUrl;

  @Enumerated(value = EnumType.STRING)
  MemberRoleType role;

  Boolean isBan;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "TOKEN_ID")
  private MemberToken token;

  @Builder
  public Member(
      LoginType loginType,
      String socialId,
      String nickName,
      String email,
      String profileImageUrl,
      MemberRoleType role,
      Boolean isBan,
      MemberToken token) {
    this.loginType = loginType;
    this.socialId = socialId;
    this.nickName = nickName;
    this.email = email;
    this.profileImageUrl = profileImageUrl;
    this.role = role;
    this.isBan = isBan;
    this.token = token;
  }

  public void updateNickName(String nickName) {
    this.nickName = nickName;
  }

  public void registerEmail(String email) {
    this.email = email;
  }

  public void updateRefreshToken(MemberToken token) {
    this.token = token;
  }

  public void deleteRefreshToken() {
    this.token = null;
  }
}

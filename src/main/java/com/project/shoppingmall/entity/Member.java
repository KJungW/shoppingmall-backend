package com.project.shoppingmall.entity;

import com.project.shoppingmall.exception.ServerLogicError;
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
public class Member extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(value = EnumType.STRING)
  private LoginType loginType;

  private String socialId;

  private String nickName;

  private String email;

  private String profileImageUrl;

  private String profileImageDownLoadUrl;

  @Enumerated(value = EnumType.STRING)
  private MemberRoleType role;

  private Boolean isBan;

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
      String profileImageDownLoadUrl,
      MemberRoleType role,
      Boolean isBan,
      MemberToken token) {
    this.loginType = loginType;
    this.socialId = socialId;
    this.nickName = nickName;
    this.email = email;
    this.profileImageUrl = profileImageUrl;
    this.profileImageDownLoadUrl = profileImageDownLoadUrl;
    this.role = role;
    updateMemberBan(isBan);
    this.token = token;
  }

  public void updateNickName(String nickName) {
    this.nickName = nickName;
  }

  public void updateProfile(String imageUri, String downloadUrl) {
    this.profileImageUrl = imageUri;
    this.profileImageDownLoadUrl = downloadUrl;
  }

  public void updateMemberBan(Boolean isBan) {
    if (isBan == null) throw new ServerLogicError("Member.isBan 필드에 비어있는 값이 입력되었습니다.");
    this.isBan = isBan;
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

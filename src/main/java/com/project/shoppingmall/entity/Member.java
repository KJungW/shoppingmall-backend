package com.project.shoppingmall.entity;

import com.project.shoppingmall.exception.ServerLogicError;
import com.project.shoppingmall.type.LoginType;
import com.project.shoppingmall.type.MemberRoleType;
import com.project.shoppingmall.util.PasswordEncoderUtil;
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

  @Column(unique = true, length = 325)
  private String email;

  private String password;

  private String profileImageUrl;

  private String profileImageDownLoadUrl;

  @Enumerated(value = EnumType.STRING)
  private MemberRoleType role;

  private Boolean isBan;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "TOKEN_ID")
  private MemberToken token;

  private String accountNumber;

  @Builder
  public Member(
      LoginType loginType,
      String socialId,
      String nickName,
      String email,
      String password,
      String profileImageUrl,
      String profileImageDownLoadUrl,
      MemberRoleType role,
      Boolean isBan,
      MemberToken token) {

    if (loginType == LoginType.EMAIL) {
      if ((email == null || email.isBlank()) || (password == null || password.isBlank()))
        throw new ServerLogicError("이메일 가입계정은 이메일과 비밀번호를 필수로 입력해야합니다.");
    }

    updateLoginType(loginType);
    this.socialId = socialId;
    updateNickName(nickName);
    this.email = email;
    updatePassword(password);
    this.profileImageUrl = profileImageUrl;
    this.profileImageDownLoadUrl = profileImageDownLoadUrl;
    updateRole(role);
    updateMemberBan(isBan);
    this.token = token;
    this.accountNumber = "";
  }

  private void updateLoginType(LoginType loginType) {
    if (loginType == null) throw new ServerLogicError("Member의 loginType필드에 빈값이 입력되었습니다.");
    this.loginType = loginType;
  }

  private void updatePassword(String password) {
    if (password == null || password.isBlank()) return;
    this.password = PasswordEncoderUtil.encodePassword(password);
  }

  public void updateNickName(String nickName) {
    if (nickName == null || nickName.isBlank())
      throw new ServerLogicError("Member의 nickName필드에 빈값이 입력되었습니다.");
    this.nickName = nickName;
  }

  public void updateRole(MemberRoleType role) {
    if (role == null) throw new ServerLogicError("Member의 role필드에 빈값이 입력되었습니다.");
    this.role = role;
  }

  public void updateProfile(String imageUri, String downloadUrl) {
    if (imageUri == null || imageUri.isBlank())
      throw new ServerLogicError("Member의 imageUri필드에 빈값이 입력되었습니다.");
    if (downloadUrl == null || downloadUrl.isBlank())
      throw new ServerLogicError("Member의 downloadUrl필드에 빈값이 입력되었습니다.");

    this.profileImageUrl = imageUri;
    this.profileImageDownLoadUrl = downloadUrl;
  }

  public void updateMemberBan(Boolean isBan) {
    if (isBan == null) throw new ServerLogicError("Member.isBan 필드에 비어있는 값이 입력되었습니다.");
    this.isBan = isBan;
  }

  public void registerEmail(String email) {
    if (email == null || email.isBlank())
      throw new ServerLogicError("Member의 email필드에 빈값이 입력되었습니다.");
    this.email = email;
  }

  public void updateRefreshToken(MemberToken token) {
    if (token == null) throw new ServerLogicError("Member의 token필드에 빈값이 입력되었습니다.");
    this.token = token;
  }

  public void deleteRefreshToken() {
    this.token = null;
  }

  public void registerAccount(String accountNumber) {
    if (accountNumber == null || accountNumber.isBlank())
      throw new ServerLogicError("Member의 accountNumber필드에 빈값이 입력되었습니다.");
    this.accountNumber = accountNumber;
  }

  public boolean checkAccountAvailable() {
    return this.accountNumber != null && !this.accountNumber.isBlank();
  }
}

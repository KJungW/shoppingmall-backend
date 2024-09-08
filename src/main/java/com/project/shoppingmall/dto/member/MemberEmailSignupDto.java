package com.project.shoppingmall.dto.member;

import lombok.Builder;
import lombok.Getter;

@Getter
public class MemberEmailSignupDto {
  private String email;
  private String password;
  private String nickName;

  @Builder
  public MemberEmailSignupDto(String email, String password, String nickName) {
    this.email = email;
    this.password = password;
    this.nickName = nickName;
  }
}

package com.project.shoppingmall.controller.member.output;

import com.project.shoppingmall.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OutputLoginByEmail {
  private Long memberId;
  private String nickName;
  private String email;
  private String profileImageDownLoadUrl;
  private Boolean isBan;

  public OutputLoginByEmail(Member member) {
    this.memberId = member.getId();
    this.nickName = member.getNickName();
    this.email = member.getEmail();
    this.profileImageDownLoadUrl = member.getProfileImageDownLoadUrl();
    this.isBan = member.getIsBan();
  }
}

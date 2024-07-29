package com.project.shoppingmall.controller.member.output;

import com.project.shoppingmall.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OutputUpdateMemberInfo {
  private Long id;
  private String nickName;
  private String email;
  private String profileImageDownLoadUrl;
  private Boolean isBan;

  public OutputUpdateMemberInfo(Member member) {
    this.id = member.getId();
    this.nickName = member.getNickName();
    this.email = member.getEmail();
    this.profileImageDownLoadUrl = member.getProfileImageDownLoadUrl();
    this.isBan = member.getIsBan();
  }
}

package com.project.shoppingmall.dto.member;

import com.project.shoppingmall.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OtherMemberDto {
  private Long id;
  private String nickName;
  private String profileImageDownLoadUrl;

  public OtherMemberDto(Member member) {
    this.id = member.getId();
    this.nickName = member.getNickName();
    this.profileImageDownLoadUrl = member.getProfileImageDownLoadUrl();
  }
}

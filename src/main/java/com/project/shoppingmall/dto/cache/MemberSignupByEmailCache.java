package com.project.shoppingmall.dto.cache;

import com.project.shoppingmall.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberSignupByEmailCache {
  private Member member;
  private String secretNumber;
}

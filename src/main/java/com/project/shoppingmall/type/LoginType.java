package com.project.shoppingmall.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LoginType {
  GOOGLE("google"),
  NAVER("naver"),
  KAKAO("kakao"),
  EMAIL("email");
  private final String registrationId;
}

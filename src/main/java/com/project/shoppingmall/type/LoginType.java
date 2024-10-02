package com.project.shoppingmall.type;

import com.project.shoppingmall.exception.ServerLogicError;
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

  public static LoginType getLoginType(String registrationId) {
    for (LoginType type : LoginType.values()) {
      if (type.getRegistrationId().equals(registrationId)) return type;
    }
    throw new ServerLogicError("registrationId에 해당하는 LoginType을 찾을 수 없습니다.");
  }
}

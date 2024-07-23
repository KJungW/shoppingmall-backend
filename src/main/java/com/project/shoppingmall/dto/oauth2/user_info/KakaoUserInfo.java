package com.project.shoppingmall.dto.oauth2.user_info;

import com.project.shoppingmall.type.LoginType;
import java.util.Map;

public class KakaoUserInfo implements OAuth2UserInfo {
  private final Map<String, Object> attribute;

  public KakaoUserInfo(Map<String, Object> attribute) {
    this.attribute = attribute;
  }

  @Override
  public LoginType getLoginType() {
    return LoginType.KAKAO;
  }

  @Override
  public String getSocialId() {
    return attribute.get("id").toString();
  }

  @Override
  public String getName() {
    Map<String, Object> properties = (Map<String, Object>) attribute.get("properties");
    return properties.get("nickname").toString();
  }
}

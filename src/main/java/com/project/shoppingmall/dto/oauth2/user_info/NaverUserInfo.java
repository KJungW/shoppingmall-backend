package com.project.shoppingmall.dto.oauth2.user_info;

import com.project.shoppingmall.type.LoginType;
import java.util.Map;

public class NaverUserInfo implements OAuth2UserInfo {

  private final Map<String, Object> attribute;

  public NaverUserInfo(Map<String, Object> attribute) {
    this.attribute = (Map<String, Object>) attribute.get("response");
  }

  @Override
  public LoginType getLoginType() {
    return LoginType.NAVER;
  }

  @Override
  public String getSocialId() {
    return attribute.get("id").toString();
  }

  @Override
  public String getName() {
    return attribute.get("name").toString();
  }
}

package com.project.shoppingmall.dto.oauth2.user_info;

import com.project.shoppingmall.type.LoginType;
import java.util.Map;

public class GoogleUserInfo implements OAuth2UserInfo {

  private final Map<String, Object> attribute;

  public GoogleUserInfo(Map<String, Object> attribute) {
    this.attribute = attribute;
  }

  @Override
  public LoginType getLoginType() {
    return LoginType.GOOGLE;
  }

  @Override
  public String getSocialId() {
    return attribute.get("sub").toString();
  }

  @Override
  public String getName() {
    return attribute.get("name").toString();
  }
}

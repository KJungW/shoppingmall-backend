package com.project.shoppingmall.dto.oauth2;

import com.project.shoppingmall.dto.oauth2.user_info.OAuth2UserInfo;
import java.util.Collection;
import java.util.Map;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Getter
public class OAuth2UserPrinciple implements OAuth2User {
  private final OAuth2UserInfo userInfo;

  public OAuth2UserPrinciple(OAuth2UserInfo userInfo) {
    this.userInfo = userInfo;
  }

  @Override
  public Map<String, Object> getAttributes() {
    return null;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return null;
  }

  @Override
  public String getName() {
    return userInfo.getName();
  }
}

package com.project.shoppingmall.dto.oauth2.user_info;

import com.project.shoppingmall.type.LoginType;

public interface OAuth2UserInfo {
  LoginType getLoginType();

  String getSocialId();

  // 사용자 실명 (설정한 이름)
  String getName();
}

package com.project.shoppingmall.service;

import com.project.shoppingmall.dto.oauth2.OAuth2UserPrinciple;
import com.project.shoppingmall.dto.oauth2.user_info.GoogleUserInfo;
import com.project.shoppingmall.dto.oauth2.user_info.KakaoUserInfo;
import com.project.shoppingmall.dto.oauth2.user_info.NaverUserInfo;
import com.project.shoppingmall.dto.oauth2.user_info.OAuth2UserInfo;
import com.project.shoppingmall.exception.OAuth2AuthenticationProcessingException;
import com.project.shoppingmall.type.LoginType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oAuth2User = super.loadUser(userRequest);
    try {
      OAuth2UserInfo oAuth2UserInfo = makeOAuthUserInfo(userRequest, oAuth2User);
      return new OAuth2UserPrinciple(oAuth2UserInfo);
    } catch (AuthenticationException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
    }
  }

  private OAuth2UserInfo makeOAuthUserInfo(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
    String registrationId = userRequest.getClientRegistration().getRegistrationId();
    OAuth2UserInfo oAuth2Response = null;
    if (registrationId.equals(LoginType.NAVER.getRegistrationId())) {
      oAuth2Response = new NaverUserInfo(oAuth2User.getAttributes());
      return oAuth2Response;
    } else if (registrationId.equals(LoginType.GOOGLE.getRegistrationId())) {
      oAuth2Response = new GoogleUserInfo(oAuth2User.getAttributes());
      return oAuth2Response;
    } else if (registrationId.equals(LoginType.KAKAO.getRegistrationId())) {
      oAuth2Response = new KakaoUserInfo(oAuth2User.getAttributes());
      return oAuth2Response;
    } else {
      throw new OAuth2AuthenticationProcessingException("지원하지 않는 oauth2 서비스입니다.");
    }
  }
}

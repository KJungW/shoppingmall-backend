package com.project.shoppingmall.service.auth;

import com.project.shoppingmall.dto.oauth2.OAuth2UserPrinciple;
import com.project.shoppingmall.dto.oauth2.user_info.GoogleUserInfo;
import com.project.shoppingmall.dto.oauth2.user_info.KakaoUserInfo;
import com.project.shoppingmall.dto.oauth2.user_info.NaverUserInfo;
import com.project.shoppingmall.dto.oauth2.user_info.OAuth2UserInfo;
import com.project.shoppingmall.exception.OAuth2AuthenticationProcessingException;
import com.project.shoppingmall.exception.ServerLogicError;
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
    OAuth2User oAuth2User = loadUserInSuperClass(userRequest);
    try {
      LoginType loginType = findLoginType(userRequest);
      OAuth2UserInfo oAuth2UserInfo = makeOAuthUserInfo(loginType, oAuth2User);
      return new OAuth2UserPrinciple(oAuth2UserInfo);
    } catch (AuthenticationException authenticationException) {
      throw authenticationException;
    } catch (Exception ex) {
      throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
    }
  }

  public OAuth2User loadUserInSuperClass(OAuth2UserRequest userRequest) {
    return super.loadUser(userRequest);
  }

  private OAuth2UserInfo makeOAuthUserInfo(LoginType loginType, OAuth2User oAuth2User) {
    return switch (loginType) {
      case NAVER -> new NaverUserInfo(oAuth2User.getAttributes());
      case GOOGLE -> new GoogleUserInfo(oAuth2User.getAttributes());
      case KAKAO -> new KakaoUserInfo(oAuth2User.getAttributes());
      default -> throw new OAuth2AuthenticationProcessingException("지원하지 않는 oauth2 서비스입니다.");
    };
  }

  private LoginType findLoginType(OAuth2UserRequest userRequest) {
    try {
      String registrationId = userRequest.getClientRegistration().getRegistrationId();
      return LoginType.getLoginType(registrationId);
    } catch (ServerLogicError error) {
      throw new OAuth2AuthenticationProcessingException("지원하지 않는 oauth2 서비스입니다.");
    }
  }
}

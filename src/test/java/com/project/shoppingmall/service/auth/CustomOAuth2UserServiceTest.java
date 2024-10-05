package com.project.shoppingmall.service.auth;

import static org.mockito.Mockito.*;

import com.project.shoppingmall.dto.oauth2.OAuth2UserPrinciple;
import com.project.shoppingmall.dto.oauth2.user_info.OAuth2UserInfo;
import com.project.shoppingmall.type.LoginType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

class CustomOAuth2UserServiceTest {

  private CustomOAuth2UserService target;

  @BeforeEach
  public void beforeEach() {
    target = spy(CustomOAuth2UserService.class);
  }

  @Test
  @DisplayName("CustomOAuth2UserService.loadUser() : 네이버 로그인 정상흐름")
  public void loadUser_naver() {
    // given
    OAuth2UserRequest inputMockUserRequest = makeMockUserRequest(LoginType.NAVER);

    String givenId = "testId";
    String givenName = "testName";
    Map<String, Object> givenResponseMap = makeNaverResponseMap(givenId, givenName);
    OAuth2User givenOAuth2User = makeMockOAuth2User(givenResponseMap);

    doReturn(givenOAuth2User).when(target).loadUserInSuperClass(any());

    // when
    OAuth2UserPrinciple result = (OAuth2UserPrinciple) target.loadUser(inputMockUserRequest);

    // then
    checkOAuth2UserPrinciple(result, givenId, givenName, LoginType.NAVER);
  }

  @Test
  @DisplayName("CustomOAuth2UserService.loadUser() : 구글 로그인 정상흐름")
  public void loadUser_google() {
    // given
    OAuth2UserRequest inputMockUserRequest = makeMockUserRequest(LoginType.GOOGLE);

    String givenId = "testId";
    String givenName = "testName";
    Map<String, Object> givenResponseMap = makeGoogleResponseMap(givenId, givenName);
    OAuth2User givenOAuth2User = makeMockOAuth2User(givenResponseMap);

    doReturn(givenOAuth2User).when(target).loadUserInSuperClass(any());

    // when
    OAuth2UserPrinciple result = (OAuth2UserPrinciple) target.loadUser(inputMockUserRequest);

    // then
    checkOAuth2UserPrinciple(result, givenId, givenName, LoginType.GOOGLE);
  }

  @Test
  @DisplayName("CustomOAuth2UserService.loadUser() : 카카오 로그인 정상흐름")
  public void loadUser_kakao() {
    // given
    OAuth2UserRequest inputMockUserRequest = makeMockUserRequest(LoginType.KAKAO);

    String givenId = "testId";
    String givenName = "testName";
    Map<String, Object> givenResponseMap = makeKakaoResponseMap(givenId, givenName);
    OAuth2User givenOAuth2User = makeMockOAuth2User(givenResponseMap);

    doReturn(givenOAuth2User).when(target).loadUserInSuperClass(any());

    // when
    OAuth2UserPrinciple result = (OAuth2UserPrinciple) target.loadUser(inputMockUserRequest);

    // then
    checkOAuth2UserPrinciple(result, givenId, givenName, LoginType.KAKAO);
  }

  public OAuth2UserRequest makeMockUserRequest(LoginType loginType) {
    ClientRegistration mockClientRegistration = mock(ClientRegistration.class);
    when(mockClientRegistration.getRegistrationId()).thenReturn(loginType.getRegistrationId());
    OAuth2UserRequest mockOAuthUserRequest = mock(OAuth2UserRequest.class);
    when(mockOAuthUserRequest.getClientRegistration()).thenReturn(mockClientRegistration);
    return mockOAuthUserRequest;
  }

  public Map<String, Object> makeNaverResponseMap(String id, String name) {
    Map<String, Object> naverAttributeMap = new HashMap<>();
    naverAttributeMap.put("id", id);
    naverAttributeMap.put("name", name);
    Map<String, Object> naverResponseMap = new HashMap<>();
    naverResponseMap.put("response", naverAttributeMap);
    return naverResponseMap;
  }

  public Map<String, Object> makeGoogleResponseMap(String id, String name) {
    Map<String, Object> googleResponseMap = new HashMap<>();
    googleResponseMap.put("sub", id);
    googleResponseMap.put("name", name);
    return googleResponseMap;
  }

  public Map<String, Object> makeKakaoResponseMap(String id, String name) {
    Map<String, Object> kakaoAttributeMap = new HashMap<>();
    kakaoAttributeMap.put("nickname", name);
    Map<String, Object> kakaoResponseMap = new HashMap<>();
    kakaoResponseMap.put("id", id);
    kakaoResponseMap.put("properties", kakaoAttributeMap);
    return kakaoResponseMap;
  }

  public OAuth2User makeMockOAuth2User(Map<String, Object> responseMap) {
    OAuth2User givenOAuth2User = mock(OAuth2User.class);
    when(givenOAuth2User.getAttributes()).thenReturn(responseMap);
    return givenOAuth2User;
  }

  public void checkOAuth2UserPrinciple(
      OAuth2UserPrinciple target, String givenId, String givenName, LoginType givenLoginType) {
    OAuth2UserInfo userInfo = target.getUserInfo();
    Assertions.assertEquals(givenId, userInfo.getSocialId());
    Assertions.assertEquals(givenName, userInfo.getName());
    Assertions.assertEquals(givenLoginType, userInfo.getLoginType());
  }
}

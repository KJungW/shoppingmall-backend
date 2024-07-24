package com.project.shoppingmall.service;

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
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

class CustomOAuth2UserServiceTest {
  private CustomOAuth2UserService testTarget;
  private OAuth2User mockOAuth2User;
  private OAuth2UserRequest mockOAuthUserRequest;
  private ClientRegistration mockClientRegistration;

  @BeforeEach
  public void beforeEach() {
    mockOAuth2User = mock(OAuth2User.class);
    mockOAuthUserRequest = mock(OAuth2UserRequest.class);
    mockClientRegistration = mock(ClientRegistration.class);
    when(mockOAuthUserRequest.getClientRegistration()).thenReturn(mockClientRegistration);

    testTarget = spy(new CustomOAuth2UserService());
    DefaultOAuth2UserService mockParent = mock(DefaultOAuth2UserService.class);
    when(mockParent.loadUser(any())).thenReturn(mockOAuth2User);
    ReflectionTestUtils.setField(testTarget, "superClass", mockParent);
  }

  @Test
  @DisplayName("CustomOAuth2UserService.loadUser() : 네이버 로그인 정상흐름")
  public void loadUser_naver() {
    // given
    Map<String, Object> naverAttributeMap = new HashMap<>();
    naverAttributeMap.put("id", "xczvjoijeqtjskldfjkljqoiwersdfa");
    naverAttributeMap.put("name", "Kim");
    Map<String, Object> naverResponseMap = new HashMap<>();
    naverResponseMap.put("response", naverAttributeMap);

    when(mockOAuth2User.getAttributes()).thenReturn(naverResponseMap);
    when(mockClientRegistration.getRegistrationId()).thenReturn("naver");

    // when
    OAuth2UserPrinciple resultUserPrinciple =
        (OAuth2UserPrinciple) testTarget.loadUser(mockOAuthUserRequest);
    OAuth2UserInfo userInfo = resultUserPrinciple.getUserInfo();

    // then
    Assertions.assertEquals("Kim", userInfo.getName());
    Assertions.assertEquals("xczvjoijeqtjskldfjkljqoiwersdfa", userInfo.getSocialId());
    Assertions.assertEquals(LoginType.NAVER, userInfo.getLoginType());
  }

  @Test
  @DisplayName("CustomOAuth2UserService.loadUser() : 구글 로그인 정상흐름")
  public void loadUser_google() {
    // given
    Map<String, Object> googleResponseMap = new HashMap<>();
    googleResponseMap.put("sub", "239018490128934123213123");
    googleResponseMap.put("name", "Kim");

    when(mockOAuth2User.getAttributes()).thenReturn(googleResponseMap);
    when(mockClientRegistration.getRegistrationId()).thenReturn("google");

    // when
    OAuth2UserPrinciple resultUserPrinciple =
        (OAuth2UserPrinciple) testTarget.loadUser(mockOAuthUserRequest);
    OAuth2UserInfo userInfo = resultUserPrinciple.getUserInfo();

    // then
    Assertions.assertEquals("Kim", userInfo.getName());
    Assertions.assertEquals("239018490128934123213123", userInfo.getSocialId());
    Assertions.assertEquals(LoginType.GOOGLE, userInfo.getLoginType());
  }

  @Test
  @DisplayName("CustomOAuth2UserService.loadUser() : 카카오 로그인 정상흐름")
  public void loadUser_kakao() {
    // given
    Map<String, Object> kakaoAttributeMap = new HashMap<>();
    kakaoAttributeMap.put("nickname", "Kim");
    Map<String, Object> kakaoResponseMap = new HashMap<>();
    kakaoResponseMap.put("id", "23019120390213");
    kakaoResponseMap.put("properties", kakaoAttributeMap);

    when(mockOAuth2User.getAttributes()).thenReturn(kakaoResponseMap);
    when(mockClientRegistration.getRegistrationId()).thenReturn("kakao");

    // when
    OAuth2UserPrinciple resultUserPrinciple =
        (OAuth2UserPrinciple) testTarget.loadUser(mockOAuthUserRequest);
    OAuth2UserInfo userInfo = resultUserPrinciple.getUserInfo();

    // then
    Assertions.assertEquals("Kim", userInfo.getName());
    Assertions.assertEquals("23019120390213", userInfo.getSocialId());
    Assertions.assertEquals(LoginType.KAKAO, userInfo.getLoginType());
  }
}

package com.project.shoppingmall.test_dto.token;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.project.shoppingmall.dto.token.RefreshAndAccessToken;

public class RefreshAndAccessTokenManager {
  public static void check(String refreshToken, String accessToken, RefreshAndAccessToken target) {
    assertEquals(refreshToken, target.getRefreshToken());
    assertEquals(accessToken, target.getAccessToken());
  }
}

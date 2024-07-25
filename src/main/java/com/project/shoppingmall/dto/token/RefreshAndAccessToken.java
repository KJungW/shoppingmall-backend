package com.project.shoppingmall.dto.token;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RefreshAndAccessToken {
  private String refreshToken;
  private String accessToken;
}

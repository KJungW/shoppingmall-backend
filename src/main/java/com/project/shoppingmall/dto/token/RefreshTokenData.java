package com.project.shoppingmall.dto.token;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RefreshTokenData {
  Long id;
  String roleType;
}

package com.project.shoppingmall.dto.token;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AccessTokenData {
  Long id;
  String roleType;
}

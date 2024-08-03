package com.project.shoppingmall.dto.token;

import com.project.shoppingmall.type.MemberRoleType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RefreshTokenData {
  Long id;
  MemberRoleType roleType;
}

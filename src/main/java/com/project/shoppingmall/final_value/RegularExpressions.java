package com.project.shoppingmall.final_value;

public class RegularExpressions {
  public static final String PRODUCT_TYPE_PATTERN = "[가-힣a-zA-Z0-9 \\-]+\\$[가-힣a-zA-Z0-9 \\-]+";
  public static final String MEMBER_PASSWORD_PATTERN =
      "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,15}$";
}
